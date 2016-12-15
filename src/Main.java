import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import group.Group;
import search.BeamSearch;
import search.quality.SensitivityQualityMeasure;
import search.quality.WeightedRelativeAccuracyQualityMeasure;
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

            /*System.out.println();

            AbstractAttribute likeAttribute = null;
            for(AbstractAttribute attribute : dataset.getAttributes()) {
                if(attribute.getName().equals("like")) {
                    likeAttribute = attribute;
                }
                System.out.println("Null cases length: " + attribute.getName() + ", " + attribute.getNullIndices().size());
            }

            System.out.println();

            if(likeAttribute == null) {
                System.out.println("Like attribute is null.");
                System.exit(9812);
            }

            HashSet<Long> groups = new HashSet<>();
            for(Object o : likeAttribute.getConstraints()) {
                Constraint constraint = (Constraint) o;
                ConfusionMatrix confusionMatrix = likeAttribute.getConfusionMatrix(constraint);
                System.out.println(constraint + ", prime=" + (constraint.getProduct()) + ", " + confusionMatrix);

                Group group = new Group().extendGroupWith(constraint, groups);
                groups.add(group.getProduct());

                group.getIndicesSubset();
            }

            System.out.println(groups.size());
            System.out.println(likeAttribute.getConstraints().size());*/

            HashSet<String> blacklist = new HashSet<>();
            blacklist.add("decision");
            blacklist.add("decision_o");

            Date start = new Date();
            GroupPriorityQueue queue = new BeamSearch(true).search(dataset, new WeightedRelativeAccuracyQualityMeasure(), new SimpleRefinementOperator(), 10, 2, 5, blacklist);
            Date end = new Date();

            for(Group group : queue) {
                System.out.println(group);
                System.out.println(group.getConfusionMatrix());
            }

            System.out.println("Starting time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(start));
            System.out.println("Ending time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(end));

            start = new Date();
            queue = new BeamSearch(false).search(dataset, new WeightedRelativeAccuracyQualityMeasure(), new SimpleRefinementOperator(), 10, 2, 5, blacklist);
            end = new Date();

            for(Group group : queue) {
                System.out.println(group);
                System.out.println(group.getConfusionMatrix());
            }

            System.out.println("Starting time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(start));
            System.out.println("Ending time: " + new SimpleDateFormat("[HH:mm:ss.SSS]").format(end));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
