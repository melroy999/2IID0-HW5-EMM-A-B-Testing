package search.result;

public class ConfusionMatrix {
    public final double p;
    public final double n;
    public final double up;
    public final double un;
    public final double P;
    public final double N;

    public ConfusionMatrix(double p, double n, double up, double un, double P, double N) {
        this.p = p;
        this.n = n;
        this.up = up;
        this.un = un;
        this.P = P;
        this.N = N;
    }

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

    public double getCoverage() {
        return p + n;
    }
}
