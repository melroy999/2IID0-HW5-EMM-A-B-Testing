import arff.Dataset;
import arff.attribute.AbstractAttribute;
import group.Comparison;
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

/**
 * Class used to start the beam search process.
 */
public class Core {
    //Parameters that define a beam search.
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
    private static String[] targetAttributes = new String[]{};
    private static String filePath = "";
    private static String[] targetValues = new String[]{};
    private static Comparison[] targetComparisons = new Comparison[]{};

    /**
     * The main starter of the beam search, that reads the parameter input and sets the appropriate values.
     *
     * @param args The list of arguments.
     */
    public static void main(String[] args) {
        if(args[0].equals("--help")) {
            printHelp();
            return;
        }

        //Reset the sieve prime counter.
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
            targetAttributes = new String[]{"match"};
            filePath = "data/dataset.arff";
            targetValues = new String[]{"1"};
            targetComparisons = new Comparison[]{Comparison.EQ};
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
                        String value = args[i + 1];
                        i++;
                        String[] split;

                        switch (v) {
                            case "d":
                                SEARCH_DEPTH = Integer.valueOf(value);
                                break;
                            case "w":
                                SEARCH_WIDTH = Integer.valueOf(value);
                                break;
                            case "target":
                                //The target values have to be formatted as follows:
                                //attribute,value,attribute2,value2, etc

                                split = value.split(",");
                                targetAttributes = new String[split.length / 2];
                                targetValues = new String[split.length / 2];
                                for(int k = 0; k < split.length / 2; k++) {
                                    targetAttributes[k] = split[k * 2];
                                    targetValues[k] = split[k * 2 + 1];
                                }

                                break;
                            case "set-length":
                                RESULT_SET_LENGTH = Integer.valueOf(value);
                                break;
                            case "target-comparison":
                                split = value.split(",");
                                targetComparisons = new Comparison[split.length];

                                for(int k = 0; k < split.length; k++) {
                                    switch (split[k].toLowerCase()) {
                                        case "eq":
                                            targetComparisons[k] = Comparison.EQ;
                                            break;
                                        case "neq":
                                            targetComparisons[k] = Comparison.NEQ;
                                            break;
                                        case "lteq":
                                            targetComparisons[k] = Comparison.LTEQ;
                                            break;
                                        case "gteq":
                                            targetComparisons[k] = Comparison.GTEQ;
                                            break;
                                        default:
                                            System.out.println("Taking EQ on default, as the given comparator " + split[k] + " could not be found.");
                                    }
                                }
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
                                switch (value.toLowerCase()) {
                                    case "sro": REFINEMENT_OPERATOR = new SimpleRefinementOperator();
                                        break;
                                    case "qro": REFINEMENT_OPERATOR = new QualityRefinementOperator();
                                        break;
                                }
                                break;
                            case "quality-measure":
                                //We have another value for the quality measure.
                                double minQuality = Double.valueOf(args[i + 1].toLowerCase());

                                switch (value.toLowerCase()) {
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
                            default:
                                System.out.println("Unknown parameter -" + value + ".");
                        }
                    }
                }
            }
        }

        assert targetAttributes.length != 0;

        System.out.println("Taking values SEARCH_DEPTH = " + SEARCH_DEPTH + ", SEARCH_WIDTH = " + SEARCH_WIDTH + ", RESULT_SET_LENGTH = " + RESULT_SET_LENGTH + ", MINIMUM_GROUP_SIZE = " + MINIMUM_GROUP_SIZE + ", MAXIMUM_FRACTION = " + MAXIMUM_FRACTION + ", USE_THREADS = " + USE_THREADS + ".");
        try {
            HashSet<String> blacklist = new HashSet<>();
            blacklist.addAll(Arrays.asList(Core.blacklist));

            //Load the data from the given data file.
            Dataset dataset = Dataset.loadARFF(filePath, countNullAsZero, targetAttributes, targetValues, targetComparisons, blacklist);
            System.out.println("P=" + dataset.getP() + ", N=" + dataset.getN() + ", P+N=" + (dataset.getP() + dataset.getN()) + ", Number of instances: " + dataset.getInstances().size());

            int uniqueValues = 0;
            for(AbstractAttribute attribute : dataset.getAttributes()) {
                uniqueValues += attribute.getConstraints().size();
            }
            System.out.println("The selected attributes contain a total of " + uniqueValues + " unique constraints.");

            //Report on information about the settings.
            System.out.println("Blacklisted attributes: " + Arrays.toString(Core.blacklist).replaceAll("(\\{|\\})",""));
            System.out.println();

            System.out.println("=======================================================================================================================================");
            System.out.println("Quality measure:\t\t\t[" + QUALITY_MEASURE.getName() + "] with minimum quality value " + QUALITY_MEASURE.getMinimumValue());
            System.out.println("Quality measure formula:\t" + QUALITY_MEASURE.getFormula());
            System.out.println("Refinement Operator:\t\t[" + REFINEMENT_OPERATOR.getName() + "]");
            System.out.println("Target group: \t\t\t[" + dataset.getTargetGroup().getReadableConstraints() + "]");
            System.out.println();
            Date start = new Date();

            //Do the beam search.
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

    /**
     * Print the priority queue in a human readable format.
     *
     * @param queue The queue to print.
     * @param start The time at which the beam search started.
     * @param end The time at which the beam search ended.
     */
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

    /**
     * Print the priority queue as a CSV file format.
     *
     * @param queue The queue to print.
     * @param start The time at which the beam search started.
     * @param end The time at which the beam search ended.
     */
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

    /**
     * Restore all the default values for the parameters.
     */
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
        targetAttributes = new String[0];
        filePath = "";
        targetValues = new String[0];
        targetComparisons = new Comparison[0];
    }

    /**
     * Print all the parameter settings, together with an explanation on what they do.
     */
    private static void printHelp() {
        System.out.println("Parameters used to instantiate a beam search: ");
        System.out.println("\t-dataset-file value: The path to the dataset file. (MANDATORY)");
        System.out.println();
        System.out.println("\t-target attribute,value,attribute2,value2,etc: The attribute with the cutoff values have to be inserted in pairs. (MANDATORY)");
        System.out.println();
        System.out.println("\t-T: Enable multi-threading. By enabling this, the program will use 8 threads during the beam search, which gives a considerable performance increase.");
        System.out.println();
        System.out.println("\t-csv: Print the result table with csv formatting. When copied, the text can be added to a csv file, through which the data can be opened in excel.");
        System.out.println();
        System.out.println("\t-null-is-zero: Consider numeric null values to have a value of 0. On default, null values are left out of the evaluation.");
        System.out.println();
        System.out.println("\t-d value: Set the beam search depth to the desired integer value. (default value: " + SEARCH_DEPTH + ")");
        System.out.println();
        System.out.println("\t-w value: Set the beam search width to the desired integer value. (default value: " + SEARCH_WIDTH + ")");
        System.out.println();
        System.out.println("\t-set-length value: The size of the returned priority queue. (default value: " + RESULT_SET_LENGTH + ")");
        System.out.println();
        System.out.println("\t-target-comparison value,value2,etc: Set the comparison mode that determines whether the instance is a match with the target value.");
        System.out.println("\t\tMust be one of the following: {EQ,NEQ,LTEQ,GTEQ}");
        System.out.println("\t\t\tEQ: " + Comparison.EQ + ", NEQ: " + Comparison.NEQ + ", LTEQ: " + Comparison.LTEQ + ", GTEQ: " + Comparison.GTEQ);
        System.out.println();
        System.out.println("\t-min-group-size value: The minimum coverage a subgroup should have. (default value: " + MINIMUM_GROUP_SIZE + ")");
        System.out.println();
        System.out.println("\t-max-group-size-fraction value: The maximum coverage that a subgroup may have relative to the size of the dataset. This value should be a fraction. (default value: " + MAXIMUM_FRACTION + ")");
        System.out.println();
        System.out.println("\t-blacklist value: A list of attributes (without spaces, seperated by commas) that should be ignored.");
        System.out.println("\tExample: \'-blacklist decision,decision_o\'");
        System.out.println();
        System.out.println("\t-refinement-operator value: The type of refinement operator to use.");
        System.out.println("\t\tMust be one of the following: {SRO,QRO} (default value: SRO)");
        System.out.println("\t\t\tSRO: Simple refinement operator, which extends the seed with all possible constraints.");
        System.out.println("\t\t\tQRO: Quality refinement operator, which iterates over all possible constraints, \n" +
                "\t\t\t\tand orders them in order of quality of the added constraint.");
        System.out.println();
        System.out.println("\t-quality-measure value value2: Set the quality measure used by the beam search.");
        System.out.println("\t\tThe first value is the name of the quality measure, which must be one of the following:");
        System.out.println("\t\t{WRA,SEN,SPEC,X2}");
        System.out.println("\t\t\tWRA: Weighted Relative Accuracy, SEN: Sensitivity, SPEC: Specificity, X2: X2 statistic");
        System.out.println("\t\tThe second value is the minimum quality that the beam search result should show.");
        System.out.println("\tThe default configuration used is -quality-measure WRA 0.02");
        System.out.println();
    }
}
