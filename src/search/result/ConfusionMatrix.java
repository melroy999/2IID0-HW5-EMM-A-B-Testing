package search.result;

public class ConfusionMatrix {
    //The covered positive and negative cases.
    public final double p;
    public final double n;

    //The positive and negative cases for which the value was unknown.
    public final double up;
    public final double un;

    //The positive and negative cases in the complete dataset.
    public final double P;
    public final double N;

    /**
     * Create a confusion matrix.
     *
     * @param p The amount of covered positive cases.
     * @param n The amount of covered negative cases.
     * @param up The amount of positive cases in the unknown values.
     * @param un The amount of negative cases in the unknown values.
     * @param P The amount of positive cases in the entire dataset.
     * @param N The amount of negative cases in the entire dataset.
     */
    public ConfusionMatrix(double p, double n, double up, double un, double P, double N) {
        this.p = p;
        this.n = n;
        this.up = up;
        this.un = un;
        this.P = P;
        this.N = N;
    }

    /**
     * Show all information that would be shown in a confusion matrix.
     *
     * @return All values, plus the required sums.
     */
    @Override
    public String toString() {
        return "ConfusionMatrix{" +
                "p=" + p +
                ", n=" + n +
                ", p+n=" + (p + n) +
                ", up=" + up +
                ", un=" + un +
                ", up+un=" + (up + un) +
                ", P=" + P +
                ", N=" + N +
                ", P+N=" + (P + N) +
                '}';
    }

    /**
     * Get the coverage within the confusion matrix.
     *
     * @return The sum of the covered positives and the covered negatives.
     */
    public double getCoverage() {
        return p + n;
    }
}
