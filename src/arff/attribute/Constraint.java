package arff.attribute;

import group.Comparison;
import util.SieveOfAtkin;

import java.util.ArrayList;
import java.util.List;

public class Constraint<T> {
    //Values denoting a constraint.
    private final T value;
    private final Comparison comparison;
    private final AbstractAttribute<T> attribute;

    //A valuePrime value used for permutation detection.
    private final long valuePrime;

    //The valuePrime that uniquely defines the comparison connected to the attribute id.
    private final long comparisonPrime;

    /**
     * Create a constraint on the attribute.
     *
     * @param value The value the constraint will be based upon.
     * @param comparison The comparator used.
     * @param attribute The attribute this constraint belongs to.
     * @param comparisonPrime The prime used for duplicate comparison checking.
     * @param valuePrime The prime used for duplicate value checking.
     */
    public Constraint(T value, Comparison comparison, AbstractAttribute<T> attribute, long comparisonPrime, long valuePrime) {
        this.value = value;
        this.comparison = comparison;
        this.attribute = attribute;

        this.valuePrime = valuePrime;
        this.comparisonPrime = comparisonPrime;
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
     * Get the attribute that is part of this constraint.
     *
     * @return The attribute.
     */
    public AbstractAttribute<T> getAttribute() {
        return attribute;
    }

    public List<Integer> getIndicesSubsetForValue(Constraint<T> constraint) {
        return attribute.getIndicesSubsetForValue(constraint);
    }

    /**
     * Get the prime number associated with this constraint.
     *
     * @return The valuePrime number.
     */
    public long getValuePrime() {
        return valuePrime;
    }

    /**
     * Get the prime number associated with the comparator in the constraint.
     *
     * @return The comparator's valuePrime number.
     */
    public long getComparisonPrime() {
        return comparisonPrime;
    }
}
