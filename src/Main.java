import arff.ARFF;

public class Main {
    public static void main(String[] args) {
        try {
            ARFF arff = ARFF.loadARFF("/dataset.arff");
            System.out.println("P=" + arff.getP() + ", N=" + arff.getN() + ", P+N=" + (arff.getP() + arff.getN()) + ", Number of instances: " + arff.getInstances().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
