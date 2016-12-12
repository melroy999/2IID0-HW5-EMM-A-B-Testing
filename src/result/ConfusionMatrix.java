package result;

public class ConfusionMatrix {
    public final double p;
    public final double n;
    public final double P;
    public final double N;

    public ConfusionMatrix(double p, double n, double p1, double n1) {
        this.p = p;
        this.n = n;
        P = p1;
        N = n1;
    }
}
