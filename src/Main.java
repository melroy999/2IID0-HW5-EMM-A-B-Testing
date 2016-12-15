import arff.Dataset;
import group.Group;
import search.BeamSearch;
import search.quality.SensitivityQualityMeasure;
import search.quality.SpecificityQualityMeasure;
import search.quality.WeightedRelativeAccuracyQualityMeasure;
import search.quality.X2QualityMeasure;
import search.refinement.AbstractRefinementOperator;
import search.refinement.QualityRefinementOperator;
import search.refinement.SimpleRefinementOperator;
import util.GroupPriorityQueue;
import util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

public class Main {
    private static final int DECIMAL_PLACES = 6;
    private static int SEARCH_DEPTH = 2;
    private static int SEARCH_WIDTH = 25;
    private static int RESULT_SET_LENGTH = 100;
    private static int MINIMUM_GROUP_SIZE = 1000;
    private static double MINIMUM_QUALITY = 0.05;
    private static double MAXIMUM_FRACTION = 0.9;

    public static void main(String[] args) {
        AbstractRefinementOperator operator = new QualityRefinementOperator();

        if(args.length < 4) {
            System.out.println("Taking default values SEARCH_DEPTH = " + SEARCH_DEPTH + ", SEARCH_WIDTH = " + SEARCH_WIDTH + ", MINIMUM_GROUP_SIZE = " + MINIMUM_GROUP_SIZE + ", MAXIMUM_FRACTION = " + MAXIMUM_FRACTION + ".");
        } else {
            SEARCH_DEPTH = Integer.valueOf(args[0]);
            SEARCH_WIDTH = Integer.valueOf(args[1]);
            MINIMUM_GROUP_SIZE = Integer.valueOf(args[2]);
            MAXIMUM_FRACTION = Integer.valueOf(args[3]);
            System.out.println("Taking values SEARCH_DEPTH = " + SEARCH_DEPTH + ", SEARCH_WIDTH = " + SEARCH_WIDTH + ", MINIMUM_GROUP_SIZE = " + MINIMUM_GROUP_SIZE + ", MAXIMUM_FRACTION = " + MAXIMUM_FRACTION + ".");
        }

        try {
            Dataset dataset = Dataset.loadARFF("/dataset.arff");
            System.out.println("P=" + dataset.getP() + ", N=" + dataset.getN() + ", P+N=" + (dataset.getP() + dataset.getN()) + ", Number of instances: " + dataset.getInstances().size());

            HashSet<String> blacklist = new HashSet<>();
            blacklist.add("decision");
            blacklist.add("decision_o");

            System.out.println("= Weighted relative accuracy ===============================================================================");
            System.out.println("Heuristic: ((p + n) / (P + N)) * (p / (p + n) - P / (P + N))");
            Date start = new Date();
            GroupPriorityQueue queue = new BeamSearch(MINIMUM_GROUP_SIZE, MAXIMUM_FRACTION, MINIMUM_QUALITY, true).search(dataset, new WeightedRelativeAccuracyQualityMeasure(), operator, SEARCH_WIDTH, SEARCH_DEPTH, RESULT_SET_LENGTH, blacklist);
            Date end = new Date();
            printQueue(queue, start, end);

            System.out.println("= Sensitivity quality measure ==============================================================================");
            System.out.println("Heuristic: p / P");
            start = new Date();
            queue = new BeamSearch(MINIMUM_GROUP_SIZE, MAXIMUM_FRACTION, MINIMUM_QUALITY, true).search(dataset, new SensitivityQualityMeasure(), operator, SEARCH_WIDTH, SEARCH_DEPTH, RESULT_SET_LENGTH, blacklist);
            end = new Date();
            printQueue(queue, start, end);

            System.out.println("= Specificity quality measure ==============================================================================");
            System.out.println("Heuristic: 1 - n / N");
            start = new Date();
            queue = new BeamSearch(MINIMUM_GROUP_SIZE, MAXIMUM_FRACTION, MINIMUM_QUALITY, true).search(dataset, new SpecificityQualityMeasure(), operator, SEARCH_WIDTH, SEARCH_DEPTH, RESULT_SET_LENGTH, blacklist);
            end = new Date();
            printQueue(queue, start, end);

            System.out.println("= x2 =======================================================================================================");
            System.out.println("Heuristic: (((p * N - P * n) * (p * N - P * n)) / (P + N)) * ((P + N) * (P + N) / (P * N * (p + n) * (P + N - p - n)))");
            start = new Date();
            queue = new BeamSearch(MINIMUM_GROUP_SIZE, MAXIMUM_FRACTION, MINIMUM_QUALITY, true).search(dataset, new X2QualityMeasure(), operator, SEARCH_WIDTH, SEARCH_DEPTH, RESULT_SET_LENGTH, blacklist);
            end = new Date();
            printQueue(queue, start, end);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printQueue(GroupPriorityQueue queue, Date start, Date end) {
        System.out.println();
        for(Group group : queue) {
            System.out.println(Util.getCurrentTimeStamp() + " (id: " + group.getProduct() + ")(eval: " + group.getEvaluation() + ") " + group.getReadableConstraints());
            System.out.println(Util.getCurrentTimeStamp() + " " + group.getConfusionMatrix());
        }
        System.out.println("Starting time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(start));
        System.out.println("Ending time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(end));
        System.out.println();
    }
}
