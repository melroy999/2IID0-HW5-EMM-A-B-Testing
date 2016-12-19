package search.quality;

/**
 * The χ2 statistic, found on page 60 in:
 *
 * J. Fürnkranz, P.A. Flach
 * ROC 'n' Rule Learning - Towards a Better Understanding of Covering Algorithms, Machine Learning 58(1):39{77, 2005.
 */
public class X2QualityMeasure extends AbstractQualityMeasure {
    /**
     * Create an quality measure.
     *
     * @param minimumValue The minimum value to check for.
     */
    public X2QualityMeasure(double minimumValue) {
        super(minimumValue);
    }

    /**
     * Evaluate the given confusion table.
     *
     * @param p The p value in the confusion table.
     * @param n The n value in the confusion table.
     * @param P The P value in the confusion table.
     * @param N The N value in the confusion table.
     * @return The search.result calculated by the heuristic.
     */
    @Override
    public double evaluate(double p, double n, double P, double N) {
        return (((p * N - P * n) * (p * N - P * n)) / (P + N)) * (((P + N) * (P + N)) / (P * N * (p + n) * (P + N - p - n)));
    }

    /**
     * Get the formula used as a string object.
     *
     * @return A copy of the formula in the evaluate function as a string.
     */
    @Override
    public String getFormula() {
        return "(((p * N - P * n) * (p * N - P * n)) / (P + N)) * (((P + N) * (P + N)) / (P * N * (p + n) * (P + N - p - n)))";
    }

    /**
     * Get the full name of a quality measure.
     *
     * @return The full name of a quality measure.
     */
    @Override
    public String getName() {
        return "χ2 statistic";
    }
}
