package group;

public enum Comparison {
    EQ, NEQ, LTEQ, GTEQ;

    @Override
    public String toString() {
        switch (this) {
            case GTEQ: return "\u2265";
            case LTEQ: return "\u2264";
            case EQ: return "=";
            default: return "\u2260";
        }
    }
}
