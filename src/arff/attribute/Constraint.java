package arff.attribute;

import arff.instance.Instance;
import group.Comparison;
import util.SieveOfAtkin;

public class Constraint<T> {
    //Values denoting a constraint.
    private final T value;
    private final Comparison comparison;

    //A prime value used for permutation detection.
    private final int prime;

    public Constraint(T value, Comparison comparison) {
        this.value = value;
        this.comparison = comparison;
        this.prime = SieveOfAtkin.getNextPrime();
    }

    /**
     * Get the value the constraint is based on.
     *
     * @return The value that is compared to within the constraint.
     */
    public T getValue() {
        return value;
    }

    /**
     * Get the comparison that is made in the constraints.
     *
     * @return The comparison made within the constraint.
     */
    public Comparison getComparison() {
        return comparison;
    }

    /**
     * Get the prime number associated with this constraint.
     *
     * @return The prime number.
     */
    public int getPrime() {
        return prime;
    }
}
