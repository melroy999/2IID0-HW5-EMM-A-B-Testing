package search.quality;

/**
 * Abstract version of a quality measure.
 */
public abstract class AbstractQualityMeasure {
    //The minimum value.
    private final double minimumValue;

    /**
     * Create an quality measure.
     *
     * @param minimumValue The minimum value to check for.
     */
    public AbstractQualityMeasure(double minimumValue) {
        this.minimumValue = minimumValue;
    }

    /**
     * Get the minimum value that the quality measure desires to be in the priority queue.
     *
     * @return The minimum value, if the evaluation is below this, the result will not be added to the priority queue.
     */
    public double getMinimumValue() {
        return minimumValue;
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
    public abstract double evaluate(double p, double n, double P, double N);

    /**
     * Get the formula used as a string object.
     *
     * @return A copy of the formula in the evaluate function as a string.
     */
    public abstract String getFormula();

    /**
     * Get the full name of a quality measure.
     *
     * @return The full name of a quality measure.
     */
    public abstract String getName();
}
