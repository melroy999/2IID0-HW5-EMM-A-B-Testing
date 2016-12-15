import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import group.Group;
import search.BeamSearch;
import search.quality.SensitivityQualityMeasure;
import search.quality.WeightedRelativeAccuracyQualityMeasure;
import search.refinement.MiddleCoverageBasedRefinement;
import search.refinement.SimpleRefinementOperator;
import search.result.ConfusionMatrix;
import util.GroupPriorityQueue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        try {
            Dataset dataset = Dataset.loadARFF("/dataset.arff");
            System.out.println("P=" + dataset.getP() + ", N=" + dataset.getN() + ", P+N=" + (dataset.getP() + dataset.getN()) + ", Number of instances: " + dataset.getInstances().size());

            HashSet<String> blacklist = new HashSet<>();
            blacklist.add("decision");
            blacklist.add("decision_o");

            Date start = new Date();
            GroupPriorityQueue queue = new BeamSearch(2, 1.0, 0, true).search(dataset, new WeightedRelativeAccuracyQualityMeasure(), new SimpleRefinementOperator(), 20, 2, 100, blacklist);
            Date end = new Date();

            for(Group group : queue) {
                System.out.println(group);
                System.out.println(group.getConfusionMatrix());
            }

            System.out.println("Starting time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(start));
            System.out.println("Ending time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(end));

            /*start = new Date();
            queue = new BeamSearch(2, 1.0, 0, true).search(dataset, new WeightedRelativeAccuracyQualityMeasure(), new SimpleRefinementOperator(), 20, 2, 100, blacklist);
            end = new Date();

            for(Group group : queue) {
                System.out.println(group);
                System.out.println(group.getConfusionMatrix());
            }

            System.out.println("Starting time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(start));
            System.out.println("Ending time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(end));

            /*start = new Date();
            queue = new BeamSearch(2, 1.0, 0, false).search(dataset, new WeightedRelativeAccuracyQualityMeasure(), new SimpleRefinementOperator(), 20, 2, 100, blacklist);
            end = new Date();

            for(Group group : queue) {
                System.out.println(group);
                System.out.println(group.getConfusionMatrix());
            }

            System.out.println("Starting time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(start));
            System.out.println("Ending time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(end));*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
