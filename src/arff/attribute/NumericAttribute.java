package arff.attribute;

import arff.instance.Instance;
import group.Comparison;

import java.util.Comparator;
import java.util.Objects;

/**
 * Representation of the numeric attribute.
 */
public class NumericAttribute extends AbstractAttribute<Double> {
    /**
     * Create an attribute.
     *
     * @param name The name of the attribute.
     * @param id The id of the attribute.
     */
    public NumericAttribute(String name, int id) {
        super(name, id);
    }

    /**
     * Get the type of the attribute.
     *
     * @return The type of the attribute.
     */
    @Override
    public Type getType() {
        return Type.NUMERIC;
    }

    /**
     * Get the value out of an instance for this particular attribute.
     *
     * @param instance The instance we want the value out of.
     * @return The value in the instance connected to this attribute.
     */
    @Override
    public Double getValue(Instance instance) {
        return (Double) instance.getValue(this);
    }

    /**
     * Get the list of comparisons used by this attribute.
     *
     * @return The list of comparisons.
     */
    @Override
    public Comparison[] getComparisons() {
        return new Comparison[]{/*Comparison.EQ, Comparison.NEQ,*/ Comparison.LTEQ, Comparison.GTEQ};
    }

    /**
     * Convert the value to the appropriate type.
     *
     * @param value The value we want to convert.
     * @return The value in the correct type.
     */
    @Override
    public Double convertValue(String value) {
        return value.equals("?") ? null : Double.valueOf(value);
    }

    /**
     * Converts the attribute to its string representation.
     *
     * @return The name of the attribute, with @attribute as prefix.
     */
    @Override
    public String toString() {
        return super.toString() + " numeric";
    }

    /**
     * Get the comparator the attribute uses.
     *
     * @return A comparator using the correct type of sorting, based on the values it contains.
     */
    @Override
    public Comparator<Instance> getComparator() {
        return (o1, o2) -> {
            Double do1 = getValue(o1);
            Double do2 = getValue(o2);

            if(do1 == null && do2 == null) {
                return 0;
            }

            //Make this 1, as we want it at the end of the list.
            if(do1 == null) {
                return 1;
            }

            if(do2 == null) {
                return -1;
            }

            return Double.compare(do1, do2);
        };
    }

    /**
     * Whether the value of the instance is contained by the constraint connected to this attribute.
     *
     * @param constraint The constraint to verify.
     * @param instance The instance to evaluate.
     * @return Whether the value in the instance connected to this attribute is contained within the constraint.
     */
    @Override
    public boolean contains(Constraint<Double> constraint, Instance instance) {
        return contains(constraint, (Double) instance.getValue(this));
    }

    /**
     * Whether the value is contained by the constraint connected to this attribute.
     *
     * @param constraint The constraint to verify.
     * @param value The value to evaluate.
     * @return Whether the value is contained within the constraint.
     */
    @Override
    public boolean contains(Constraint<Double> constraint, Double value) {
        Double constraintValue = constraint.getValue();

        if(value == null) {
            if(isCountNullAsZero()) {
                value = 0d;
            } else {
                return false;
            }
        }

        switch (constraint.getComparison()) {
            case EQ:
                return value.equals(constraintValue);
            case NEQ:
                return !value.equals(constraintValue);
            case LTEQ:
                return value <= constraintValue;
            case GTEQ:
                return value >= constraintValue;
        }
        return false;
    }
}
