package arff.instance;

import arff.attribute.AbstractAttribute;
import com.sun.deploy.util.BlackList;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.HashSet;
import java.util.List;

/**
 * Class representing an instance within the Dataset file.
 */
public class Instance {
    //The id of the instance.
    private final int id;

    //The list of values, which is of the type Object such that we can cast to the correct type later on.
    private final Object[] values;

    /**
     * Create an instance.
     *
     * @param id The unique identifier of the instance.
     * @param line The line the instance is contained in.
     * @param attributes The list of attributes that the instance is composed of.
     * @param blacklist The blacklisted attributes.
     * @param offset The index offset,  as we removed some attributes before initializing, thus de ids do not match.
     */
    public Instance(int id, String line, List<AbstractAttribute> attributes, HashSet<String> blacklist, List<Integer> offset) {
        this.id = id;
        String[] stringValues = line.split(",");
        values = new Object[stringValues.length - blacklist.size()];

        //Convert the string representations of values to the appropriate type.
        for(AbstractAttribute attribute : attributes) {
            //The index of the attribute.
            int i = attribute.getId();

            //Make sure that the object is saved as the appropriate type, i.e. String, Double etc.
            values[i] = attribute.convertValue(stringValues[i + offset.get(i)]);
        }
    }

    /**
     * Get the id of the instance.
     *
     * @return The id of the instance.
     */
    public int getId() {
        return id;
    }

    /**
     * Get the value for the given attribute.
     *
     * @param attribute The attribute we want the value of.
     * @return The value in the string array at the index of the attribute.
     */
    public Object getValue(AbstractAttribute attribute) {
        return values[attribute.getId()];
    }
}
