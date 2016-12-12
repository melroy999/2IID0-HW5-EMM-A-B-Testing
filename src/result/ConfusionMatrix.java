package result;

public class ConfusionMatrix {
    public final double p;
    public final double n;
    public final double up;
    public final double un;
    public final double P;
    public final double N;

    public ConfusionMatrix(double p, double n, double up, double un, double p1, double n1) {
        this.p = p;
        this.n = n;
        this.up = up;
        this.un = un;
        P = p1;
        N = n1;
    }

    @Override
    public String toString() {
        return "ConfusionMatrix{" +
                "p=" + p +
                ", n=" + n +
                ", up=" + up +
                ", un=" + un +
                ", P=" + P +
                ", N=" + N +
                '}';
    }
}
