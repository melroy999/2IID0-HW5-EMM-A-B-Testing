package result.quality;

/**
 * Abstract version of a quality measure.
 */
public abstract class AbstractQualityMeasure {
    /**
     * Evaluate the given confusion table.
     *
     * @param p The p value in the confusion table.
     * @param n The n value in the confusion table.
     * @param P The P value in the confusion table.
     * @param N The N value in the confusion table.
     * @return The result calculated by the heuristic.
     */
    public abstract double evaluate(double p, double n, double P, double N);
}
