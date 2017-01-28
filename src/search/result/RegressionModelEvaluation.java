package search.result;

import java.util.Arrays;

public class RegressionModelEvaluation {
    public final double evaluationValue;
    public final double[] estimators;
    public final int size;

    public RegressionModelEvaluation(double evaluationValue, double[] estimators, int size) {
        this.evaluationValue = evaluationValue;
        this.estimators = estimators;
        this.size = size;
    }

    @Override
    public String toString() {
        String result = "value = " + evaluationValue + ", estimators = {";

        boolean isfirst = true;
        for(double estimator : estimators) {
            if(!isfirst) result += ", ";

            result += round(estimator, 4);

            isfirst = false;
        }

        return result + "}, size of group = " + size;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
