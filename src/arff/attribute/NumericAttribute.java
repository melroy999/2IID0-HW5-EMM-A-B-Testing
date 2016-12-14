package arff.attribute;

import arff.instance.Instance;
import group.Comparison;

import java.util.Comparator;

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

    @Override
    public boolean contains(Constraint<Double> constraint, Instance instance) {
        switch (constraint.getComparison()) {
            case EQ:
                return instance.getValue(this) == constraint.getValue();
            case NEQ:
                return instance.getValue(this) != constraint.getValue();
            case LTEQ:
                return (double) instance.getValue(this) <= constraint.getValue();
            case GTEQ:
                return (double) instance.getValue(this) >= constraint.getValue();
        }
        return false;
    }
}
