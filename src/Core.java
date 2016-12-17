import arff.Dataset;
import group.Group;
import search.BeamSearch;
import search.quality.*;
import search.refinement.AbstractRefinementOperator;
import search.refinement.QualityRefinementOperator;
import search.refinement.SimpleRefinementOperator;
import util.GroupPriorityQueue;
import util.SieveOfAtkin;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class Core {
    private static final int DECIMAL_PLACES = 6;
    private static int SEARCH_DEPTH = 1;
    private static int SEARCH_WIDTH = 10;
    private static int RESULT_SET_LENGTH = 100;
    private static int MINIMUM_GROUP_SIZE = 2;
    private static double MAXIMUM_FRACTION = 1.0;
    private static String[] blacklist = new String[]{};
    private static boolean USE_THREADS = false;
    private static AbstractRefinementOperator REFINEMENT_OPERATOR = new QualityRefinementOperator();
    private static AbstractQualityMeasure QUALITY_MEASURE = new WeightedRelativeAccuracyQualityMeasure(0.02);
    private static boolean printCSVFormat = false;
    private static boolean countNullAsZero = false;
    private static String targetAttribute = "";
    private static String filePath = "";

    public static void main(String[] args) {
        SieveOfAtkin.resetCounter();

        //Set default values.
        restoreDefaults();

        if(args.length == 0) {
            //Set the default values.
            USE_THREADS = true;
            MAXIMUM_FRACTION = 0.9;
            MINIMUM_GROUP_SIZE = 1000;
            SEARCH_DEPTH = 2;
            SEARCH_WIDTH = 25;
            REFINEMENT_OPERATOR = new QualityRefinementOperator();
            QUALITY_MEASURE = new WeightedRelativeAccuracyQualityMeasure(0.02);
            blacklist = new String[]{"decision","decision_o"};
            countNullAsZero = false;
            targetAttribute = "match";
            filePath = "data/dataset.arff";
        } else {
            System.out.println("Arguments: " + Arrays.toString(args));
            for(int i = 0; i < args.length; i++) {
                String arg = args[i];
                if(arg.startsWith("-")) {
                    //Find the value we want to set.
                    String v = arg.replaceFirst("-", "");

                    if(v.equalsIgnoreCase("T")) {
                        USE_THREADS = true;
                    } else if(v.equalsIgnoreCase("csv")) {
                        printCSVFormat = true;
                    } else if(v.equalsIgnoreCase("null-is-zero")) {
                        countNullAsZero = true;
                    } else {
                        //Get the next value.
                        //Increment i as well.
                        String value = args[i + 1].toLowerCase();
                        i++;

                        switch (v) {
                            case "d":
                                SEARCH_DEPTH = Integer.valueOf(value);
                                break;
                            case "w":
                                SEARCH_WIDTH = Integer.valueOf(value);
                                break;
                            case "target":
                                targetAttribute = value;
                                break;
                            case "set-length":
                                RESULT_SET_LENGTH = Integer.valueOf(value);
                                break;
                            case "min-group-size":
                                MINIMUM_GROUP_SIZE = Integer.valueOf(value);
                                break;
                            case "max-group-size-fraction":
                                MAXIMUM_FRACTION = Double.valueOf(value);
                                break;
                            case "blacklist":
                                blacklist = value.split(",");
                                break;
                            case "dataset-file":
                                filePath = value;
                                break;
                            case "refinement-operator":
                                switch (value) {
                                    case "sro": REFINEMENT_OPERATOR = new SimpleRefinementOperator();
                                        break;
                                    case "qro": REFINEMENT_OPERATOR = new QualityRefinementOperator();
                                        break;
                                }
                                break;
                            case "quality-measure":
                                //We have another value for the quality measure.
                                double minQuality = Double.valueOf(args[i + 1].toLowerCase());

                                switch (value) {
                                    case "wra": QUALITY_MEASURE = new WeightedRelativeAccuracyQualityMeasure(minQuality);
                                        break;
                                    case "sen": QUALITY_MEASURE = new SensitivityQualityMeasure(minQuality);
                                        break;
                                    case "spec": QUALITY_MEASURE = new SpecificityQualityMeasure(minQuality);
                                        break;
                                    case "x2": QUALITY_MEASURE = new X2QualityMeasure(minQuality);
                                        break;
                                }

                                i++;
                                break;
                        }
                    }
                }
            }
        }

        assert !targetAttribute.equals("");

        System.out.println("Taking values SEARCH_DEPTH = " + SEARCH_DEPTH + ", SEARCH_WIDTH = " + SEARCH_WIDTH + ", RESULT_SET_LENGTH = " + RESULT_SET_LENGTH + ", MINIMUM_GROUP_SIZE = " + MINIMUM_GROUP_SIZE + ", MAXIMUM_FRACTION = " + MAXIMUM_FRACTION + ", USE_THREADS = " + USE_THREADS + ".");
        try {
            HashSet<String> blacklist = new HashSet<>();
            blacklist.addAll(Arrays.asList(Core.blacklist));

            Dataset dataset = Dataset.loadARFF(filePath, countNullAsZero, targetAttribute, blacklist);
            System.out.println("P=" + dataset.getP() + ", N=" + dataset.getN() + ", P+N=" + (dataset.getP() + dataset.getN()) + ", Number of instances: " + dataset.getInstances().size());

            System.out.println("Blacklisted attributes: " + Arrays.toString(Core.blacklist).replaceAll("(\\{|\\})",""));
            System.out.println();

            System.out.println("=======================================================================================================================================");
            System.out.println("Quality measure:\t\t\t[" + QUALITY_MEASURE.getName() + "] with minimum quality value " + QUALITY_MEASURE.getMinimumValue());
            System.out.println("Quality measure formula:\t" + QUALITY_MEASURE.getFormula());
            System.out.println("Refinement Operator:\t\t[" + REFINEMENT_OPERATOR.getName() + "]");
            System.out.println("Target attribute: \t\t\t[" + dataset.getTargetAttribute().getName() + "]");
            System.out.println();
            Date start = new Date();
            GroupPriorityQueue queue = new BeamSearch(MINIMUM_GROUP_SIZE, MAXIMUM_FRACTION, USE_THREADS).search(dataset, QUALITY_MEASURE, REFINEMENT_OPERATOR, SEARCH_WIDTH, SEARCH_DEPTH, RESULT_SET_LENGTH);
            Date end = new Date();

            if(printCSVFormat) {
                printQueueCSV(queue, start, end);
            } else {
                printQueue(queue, start, end);
            }
            System.out.println("=======================================================================================================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printQueue(GroupPriorityQueue queue, Date start, Date end) {
        System.out.println();
        System.out.println("Resulting subgroups: ");
        int i = 1;
        for(Group group : queue) {
            System.out.println(i + ". (id: " + group.getProduct() + ") " + group);
            System.out.println(" \t" + group.getConfusionMatrix());
            i++;
        }
        System.out.println();
        System.out.println("Starting time: \t" + new SimpleDateFormat("[HH:mm:ss.SSS]").format(start));
        System.out.println("Ending time: \t" + new SimpleDateFormat("[HH:mm:ss.SSS]").format(end));
        System.out.println();
    }

    private static void printQueueCSV(GroupPriorityQueue queue, Date start, Date end) {
        System.out.println();
        System.out.println("Resulting table: ");
        System.out.println("Nr.;Depth;Coverage;Quality;Positives;Conditions;");
        int i = 1;
        for(Group group : queue) {
            System.out.println(i + ";" + group.getConstraints().size() + ";" + group.getConfusionMatrix().getCoverage() + ";" + group.getEvaluation() + ";" + group.getConfusionMatrix().p + ";" + group.getReadableConstraints() + ";");
            i++;
        }
        System.out.println();
        System.out.println("Starting time: \t" + new SimpleDateFormat("[HH:mm:ss.SSS]").format(start));
        System.out.println("Ending time: \t" + new SimpleDateFormat("[HH:mm:ss.SSS]").format(end));
        System.out.println();
    }

    private static void restoreDefaults() {
        SEARCH_DEPTH = 1;
        SEARCH_WIDTH = 10;
        RESULT_SET_LENGTH = 100;
        MINIMUM_GROUP_SIZE = 2;
        MAXIMUM_FRACTION = 1.0;
        blacklist = new String[]{};
        USE_THREADS = false;
        REFINEMENT_OPERATOR = new QualityRefinementOperator();
        QUALITY_MEASURE = new WeightedRelativeAccuracyQualityMeasure(0.02);
        printCSVFormat = false;
        countNullAsZero = false;
        targetAttribute = "";
        filePath = "";
    }
}
