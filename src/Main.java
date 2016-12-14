import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import group.Group;
import search.result.ConfusionMatrix;

import java.util.Arrays;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        try {
            Dataset dataset = Dataset.loadARFF("/dataset.arff");
            System.out.println("P=" + dataset.getP() + ", N=" + dataset.getN() + ", P+N=" + (dataset.getP() + dataset.getN()) + ", Number of instances: " + dataset.getInstances().size());

            AbstractAttribute likeAttribute = null;
            for(AbstractAttribute attribute : dataset.getAttributes()) {
                if(attribute.getName().equals("like")) {
                    likeAttribute = attribute;
                }
                System.out.println("Null cases length: " + attribute.getName() + ", " + attribute.getNullIndices().size());
            }

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
            System.out.println(likeAttribute.getConstraints().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
