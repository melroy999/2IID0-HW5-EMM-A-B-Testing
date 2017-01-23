package group;

/**
 * Enum representing different comparison possibilities.
 */
public enum Comparison {
    EQ, NEQ, LTEQ, GTEQ;

    /**
     * Print the comparison as a unicode symbol.
     *
     * @return The corresponding unicode symbol.
     */
    @Override
    public String toString() {
        switch (this) {
            case GTEQ: return ">=";
            case LTEQ: return "<=";
            case EQ: return "=";
            default: return "!=";
        }
    }
}
