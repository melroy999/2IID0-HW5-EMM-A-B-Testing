package arff.attribute;

import arff.instance.Instance;
import group.Comparison;

import java.util.Comparator;
import java.util.UUID;

/**
 * Representation of a UUID attribute.
 */
public class UUIDAttribute extends AbstractAttribute<UUID> {
    /**
     * Create an attribute.
     *
     * @param name The name of the attribute.
     * @param id   The id of the attribute.
     */
    public UUIDAttribute(String name, int id) {
        super(name, id);
    }

    /**
     * Get the type of the attribute.
     *
     * @return The type of the attribute.
     */
    @Override
    public Type getType() {
        return Type.UUID;
    }

    /**
     * Get the value out of an instance for this particular attribute.
     *
     * @param instance The instance we want the value out of.
     * @return The value in the instance connected to this attribute.
     */
    @Override
    public UUID getValue(Instance instance) {
        return (UUID) instance.getValue(this);
    }

    /**
     * Get the list of comparisons used by this attribute.
     *
     * @return The list of comparisons.
     */
    @Override
    public Comparison[] getComparisons() {
        return new Comparison[]{Comparison.GTEQ, Comparison.LTEQ};
    }

    /**
     * Convert the value to the appropriate type.
     *
     * @param value The value we want to convert.
     * @return The value in the correct type.
     */
    @Override
    public UUID convertValue(String value) {
        return value.equals("?") ? null : UUID.fromString(value);
    }

    /**
     * Get the comparator the attribute uses.
     *
     * @return A comparator using the correct type of sorting, based on the values it contains.
     */
    @Override
    public Comparator<Instance> getComparator() {
        return new Comparator<Instance>() {
            @Override
            public int compare(Instance o1, Instance o2) {
                UUID uuid1 = (UUID) o1.getValue(UUIDAttribute.this);
                UUID uuid2 = (UUID) o2.getValue(UUIDAttribute.this);

                if(uuid1 == null && uuid2 == null) {
                    return 0;
                }

                //Make this 1, as we want it at the end of the list.
                if(uuid1 == null) {
                    return 1;
                }

                if(uuid2 == null) {
                    return -1;
                }

                return uuid1.compareTo(uuid2);
            }
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
    public boolean contains(Constraint<UUID> constraint, Instance instance) {
        return contains(constraint, (UUID) instance.getValue(this));
    }

    /**
     * Whether the value is contained by the constraint connected to this attribute.
     *
     * @param constraint The constraint to verify.
     * @param value The value to evaluate.
     * @return Whether the value is contained within the constraint.
     */
    @Override
    public boolean contains(Constraint<UUID> constraint, UUID value) {
        UUID constraintValue = constraint.getValue();

        if(value == null) {
            return false;
        }

        switch (constraint.getComparison()) {
            case EQ:
                return value.equals(constraintValue);
            case NEQ:
                return !value.equals(constraintValue);
            case LTEQ:
                return value.compareTo(constraintValue) <= 0;
            case GTEQ:
                return value.compareTo(constraintValue) >= 0;
        }
        return false;
    }
}
