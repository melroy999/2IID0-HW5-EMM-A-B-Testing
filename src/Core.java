import arff.Dataset;
import arff.attribute.AbstractAttribute;
import group.Group;
import search.BeamSearch;
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
    private static int SEARCH_DEPTH = 1;
    private static int SEARCH_WIDTH = 10;
    private static int RESULT_SET_LENGTH = 100;
    private static int MINIMUM_GROUP_SIZE = 2;
    private static double MAXIMUM_FRACTION = 1.0;
    private static String[] blacklist = new String[]{};
    private static boolean USE_THREADS = false;
    private static AbstractRefinementOperator REFINEMENT_OPERATOR = new QualityRefinementOperator();
    private static boolean countNullAsZero = false;
    private static String filePath = "";

    private static String yTarget;
    private static String[] xTargets;

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
            //<editor-fold desc="Default initialization">
            //Set the default values.
            USE_THREADS = true;
            MAXIMUM_FRACTION = 0.9;
            MINIMUM_GROUP_SIZE = 1000;
            SEARCH_DEPTH = 2;
            SEARCH_WIDTH = 25;
            REFINEMENT_OPERATOR = new QualityRefinementOperator();
            blacklist = new String[]{"decision","decision_o"};
            countNullAsZero = false;
            filePath = "data/speed_dating_altered.arff";
            yTarget = "like";
            xTargets = new String[]{"attractive_partner","sincere_partner","intelligence_partner","funny_partner","ambition_partner","shared_interests_partner"};
            //</editor-fold>
        } else {
            //<editor-fold desc="Parameterized initialization">
            System.out.println("Arguments: " + Arrays.toString(args));
            for(int i = 0; i < args.length; i++) {
                String arg = args[i];
                if(arg.startsWith("-")) {
                    //Find the value we want to set.
                    String v = arg.replaceFirst("-", "");

                    if(v.equalsIgnoreCase("T")) {
                        USE_THREADS = true;
                    } else if(v.equalsIgnoreCase("null-is-zero")) {
                        countNullAsZero = true;
                    } else {
                        //Get the next value.
                        //Increment i as well.
                        String value = args[i + 1];
                        i++;

                        switch (v) {
                            case "d":
                                SEARCH_DEPTH = Integer.valueOf(value);
                                break;
                            case "w":
                                SEARCH_WIDTH = Integer.valueOf(value);
                                break;
                            case "y-target":
                                yTarget = value;
                                break;
                            case "x-targets":
                                xTargets = value.split(",");
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
                                switch (value.toLowerCase()) {
                                    case "sro": REFINEMENT_OPERATOR = new SimpleRefinementOperator();
                                        break;
                                    case "qro": REFINEMENT_OPERATOR = new QualityRefinementOperator();
                                        break;
                                }
                                break;
                            default:
                                System.out.println("Unknown parameter -" + value + ".");
                        }
                    }
                }
            }
            //</editor-fold>
        }

        System.out.println("Taking values SEARCH_DEPTH = " + SEARCH_DEPTH + ", SEARCH_WIDTH = " + SEARCH_WIDTH + ", RESULT_SET_LENGTH = " + RESULT_SET_LENGTH + ", MINIMUM_GROUP_SIZE = " + MINIMUM_GROUP_SIZE + ", MAXIMUM_FRACTION = " + MAXIMUM_FRACTION + ", USE_THREADS = " + USE_THREADS + ".");
        try {
            HashSet<String> blacklist = new HashSet<>();
            blacklist.addAll(Arrays.asList(Core.blacklist));

            //Load the data from the given data file.
            Dataset dataset = Dataset.loadARFF(filePath, yTarget, xTargets, countNullAsZero, blacklist);
            System.out.println("Number of instances: " + dataset.getInstances().size());

            int uniqueValues = 0;
            for(AbstractAttribute attribute : dataset.getAttributes()) {
                uniqueValues += attribute.getConstraints().size();
            }
            System.out.println("The selected attributes contain a total of " + uniqueValues + " unique constraints.");

            //Report on information about the settings.
            System.out.println("Blacklisted attributes: " + Arrays.toString(Core.blacklist).replaceAll("(\\{|\\})",""));
            System.out.println();

            System.out.println("=======================================================================================================================================");
            System.out.println("Refinement Operator:\t\t[" + REFINEMENT_OPERATOR.getName() + "]");
            System.out.println("y target: \t\t\t\t\t[" + yTarget + "]");
            System.out.println("x targets: \t\t\t\t\t[" + Arrays.toString(xTargets).replaceAll("[|]", "") + "]");
            System.out.println();
            Date start = new Date();

            //Do the beam search.
            GroupPriorityQueue queue = new BeamSearch(MINIMUM_GROUP_SIZE, MAXIMUM_FRACTION, USE_THREADS).search(dataset, REFINEMENT_OPERATOR, SEARCH_WIDTH, SEARCH_DEPTH, RESULT_SET_LENGTH);
            Date end = new Date();

            printQueue(queue, start, end);
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
        countNullAsZero = false;
        filePath = "data/speed_dating_altered.arff";
        yTarget = "like";
        xTargets = new String[]{"attractive_partner","sincere_partner","intelligence_partner","funny_partner","ambition_partner","shared_interests_partner"};
    }

    /**
     * Print all the parameter settings, together with an explanation on what they do.
     */
    private static void printHelp() {
        System.out.println("Parameters used to instantiate a beam search: ");
        System.out.println("\t-dataset-file value: The path to the dataset file. (MANDATORY)");
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
        System.out.println("\t-min-group-size value: The minimum coverage a subgroup should have. (default value: " + MINIMUM_GROUP_SIZE + ")");
        System.out.println();
        System.out.println("\t-max-group-size-fraction value: The maximum coverage that a subgroup may have relative to the size of the dataset. This value should be a fraction. (default value: " + MAXIMUM_FRACTION + ")");
        System.out.println();
        System.out.println("\t-blacklist value: A list of attributes (without spaces, separated by commas) that should be ignored.");
        System.out.println("\tExample: \'-blacklist decision,decision_o\'");
        System.out.println();
        System.out.println("\t-refinement-operator value: The type of refinement operator to use.");
        System.out.println("\t\tMust be one of the following: {SRO,QRO} (default value: SRO)");
        System.out.println("\t\t\tSRO: Simple refinement operator, which extends the seed with all possible constraints.");
        System.out.println("\t\t\tQRO: Quality refinement operator, which iterates over all possible constraints, \n" +
                "\t\t\t\tand orders them in order of quality of the added constraint.");
    }
}
