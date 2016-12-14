package arff.attribute;

import arff.instance.Instance;
import group.Comparison;

import java.util.Comparator;

/**
 * Representation of a boolean attribute.
 */
public class BooleanAttribute extends AbstractAttribute<Boolean> {
    /**
     * Create an attribute.
     *
     * @param name The name of the attribute.
     * @param id The id of the attribute.
     */
    public BooleanAttribute(String name, int id) {
        super(name, id);
    }

    /**
     * Get the type of the attribute.
     *
     * @return The type of the attribute.
     */
    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    /**
     * Get the value out of an instance for this particular attribute.
     *
     * @param instance The instance we want the value out of.
     * @return The value in the instance connected to this attribute.
     */
    @Override
    public Boolean getValue(Instance instance) {
        return (Boolean) instance.getValue(this);
    }

    /**
     * Get the list of comparisons used by this attribute.
     *
     * @return The list of comparisons.
     */
    @Override
    public Comparison[] getComparisons() {
        return new Comparison[]{Comparison.EQ/*, Comparison.NEQ*/};
    }

    /**
     * Convert the value to the appropriate type.
     *
     * @param value The value we want to convert.
     * @return The value in the correct type.
     */
    @Override
    public Boolean convertValue(String value) {
        return value.equals("?") ? null : value.equals("1");
    }

    /**
     * Converts the attribute to its string representation.
     *
     * @return The name of the attribute, with @attribute as prefix.
     */
    @Override
    public String toString() {
        return super.toString() + " {0,1}";
    }

    /**
     * Get the comparator the attribute uses.
     *
     * @return A comparator using the correct type of sorting, based on the values it contains.
     */
    @Override
    public Comparator<Instance> getComparator() {
        return (o1, o2) -> {
            Boolean bo1 = getValue(o1);
            Boolean bo2 = getValue(o2);

            if(bo1 == null && bo2 == null) {
                return 0;
            }

            //Make this 1, as we want it at the end of the list.
            if(bo1 == null) {
                return 1;
            }

            if(bo2 == null) {
                return -1;
            }

            return Boolean.compare(bo1, bo2);
        };
    }

    /**
     * Whether the instance matches the target value.
     *
     * @param instance The instance that has to be checked.
     * @return Whether the instance target value matches the overall target value.
     */
    @Override
    public boolean matchesTargetValue(Instance instance) {
        return getValue(instance) == getTargetValue();
    }
}
