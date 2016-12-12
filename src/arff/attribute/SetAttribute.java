package arff.attribute;

import arff.instance.Instance;
import group.Comparison;

import java.util.Comparator;

/**
 * Representation of a set attribute.
 */
public class SetAttribute extends AbstractAttribute<String> {
    //The list of pre-defined values that are mentioned for this attribute.
    private final String valuesString;

    /**
     * Create an attribute.
     *
     * @param name The name of the attribute.
     * @param id The id of the attribute.
     * @param valuesString The list of values pre-defined for this attribute.
     */
    public SetAttribute(String name, int id, String valuesString) {
        super(name, id);
        this.valuesString = valuesString;
    }

    /**
     * Get the type of the attribute.
     *
     * @return The type of the attribute.
     */
    @Override
    public Type getType() {
        return Type.SET;
    }

    /**
     * Get the value out of an instance for this particular attribute.
     *
     * @param instance The instance we want the value out of.
     * @return The value in the instance connected to this attribute.
     */
    @Override
    public String getValue(Instance instance) {
        return (String) instance.getValue(this);
    }

    /**
     * Get the list of comparisons used by this attribute.
     *
     * @return The list of comparisons.
     */
    @Override
    public Comparison[] getComparisons() {
        return new Comparison[]{Comparison.EQ, Comparison.NEQ};
    }

    /**
     * Convert the value to the appropriate type.
     *
     * @param value The value we want to convert.
     * @return The value in the correct type.
     */
    @Override
    public String convertValue(String value) {
        return value.equals("?") ? null : value;
    }

    /**
     * Converts the attribute to its string representation.
     *
     * @return The name of the attribute, with @attribute as prefix.
     */
    @Override
    public String toString() {
        return super.toString() + " " + valuesString;
    }

    /**
     * Get the comparator the attribute uses.
     *
     * @return A comparator using the correct type of sorting, based on the values it contains.
     */
    @Override
    public Comparator<Instance> getComparator() {
        return (o1, o2) -> {
            String so1 = getValue(o1);
            String so2 = getValue(o2);

            if(so1 == null && so2 == null) {
                return 0;
            }

            if(so1 == null) {
                return -1;
            }

            if(so2 == null) {
                return 1;
            }

            return so1.compareTo(so2);
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
        return getTargetValue().equals(instance.getTargetValue());
    }
}
