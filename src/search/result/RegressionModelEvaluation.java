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
        return "value = " + evaluationValue + ", estimators = " + Arrays.toString(estimators) + ", size of group = " + size;
    }
}
