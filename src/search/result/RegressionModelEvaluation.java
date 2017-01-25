package search.result;

import java.util.Arrays;

public class RegressionModelEvaluation {
    public final double evaluationValue;
    public final double[] estimators;

    public RegressionModelEvaluation(double evaluationValue, double[] estimators) {
        this.evaluationValue = evaluationValue;
        this.estimators = estimators;
    }

    @Override
    public String toString() {
        return "value = " + evaluationValue + ", estimators = " + Arrays.toString(estimators);
    }
}
