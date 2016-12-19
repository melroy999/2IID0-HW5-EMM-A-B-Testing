package arff.attribute;

import arff.instance.Instance;
import group.Comparison;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Representation of a Date attribute.
 */
public class DateAttribute extends AbstractAttribute<Date> {
    public final static SimpleDateFormat format = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss.SSSSSS''");
    /**
     * Create an attribute.
     *
     * @param name The name of the attribute.
     * @param id   The id of the attribute.
     */
    public DateAttribute(String name, int id) {
        super(name, id);
    }

    /**
     * Get the type of the attribute.
     *
     * @return The type of the attribute.
     */
    @Override
    public Type getType() {
        return Type.DATE;
    }

    /**
     * Get the value out of an instance for this particular attribute.
     *
     * @param instance The instance we want the value out of.
     * @return The value in the instance connected to this attribute.
     */
    @Override
    public Date getValue(Instance instance) {
        return (Date) instance.getValue(this);
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
    public Date convertValue(String value) {
        try {
            return format.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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
                Date d1 = (Date) o1.getValue(DateAttribute.this);
                Date d2 = (Date) o2.getValue(DateAttribute.this);
                return d1.compareTo(d2);
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
    public boolean contains(Constraint<Date> constraint, Instance instance) {
        return contains(constraint, (Date) instance.getValue(this));
    }

    /**
     * Whether the value is contained by the constraint connected to this attribute.
     *
     * @param constraint The constraint to verify.
     * @param value The value to evaluate.
     * @return Whether the value is contained within the constraint.
     */
    @Override
    public boolean contains(Constraint<Date> constraint, Date value) {
        Date constraintValue = constraint.getValue();

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
