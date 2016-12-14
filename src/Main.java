import arff.Dataset;
import arff.attribute.AbstractAttribute;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        try {
            Dataset dataset = Dataset.loadARFF("/dataset.arff");
            System.out.println("P=" + dataset.getP() + ", N=" + dataset.getN() + ", P+N=" + (dataset.getP() + dataset.getN()) + ", Number of instances: " + dataset.getInstances().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
