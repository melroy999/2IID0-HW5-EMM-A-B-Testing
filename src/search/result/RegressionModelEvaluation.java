package search.result;

public class RegressionModelEvaluation {
    public final double evaluationValue;
    public final double[] estimators;

    public RegressionModelEvaluation(double evaluationValue, double[] estimators) {
        this.evaluationValue = evaluationValue;
        this.estimators = estimators;
    }
}
