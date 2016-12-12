package arff;

import arff.attribute.AbstractAttribute;
import arff.instance.Instance;

import java.util.List;

public class ARFF {
    //The list of attributes within the ARFF file.
    private final List<AbstractAttribute> attributes;

    //The list of instances within the ARFF file.
    private final List<Instance> instances;

    //The amount of positive and negative cases within the total dataset.
    private final int P;
    private final int N;

    //The target attribute.
    private final AbstractAttribute targetAttribute;

    public ARFF(List<AbstractAttribute> attributes, List<Instance> instances, int targetId, String targetValue) {
        this.instances = instances;
        this.attributes = attributes;
        this.targetAttribute = attributes.get(targetId);
        this.targetAttribute.setTargetValue(targetValue);

        //Intermediary counters.
        int P = 0;
        int N = 0;

        //Count the amount of positive and negative cases.
        for(Instance instance : instances) {
            if(targetAttribute.matchesTargetValue(instance)) {
                P++;
            } else {
                N++;
            }
        }

        //Set the P and N values.
        this.P = P;
        this.N = N;

        //Initialize all the attributes.
        for(AbstractAttribute attribute : attributes) {
            attribute.initialize(this);
        }
    }

    /**
     * Get the list of attributes.
     *
     * @return The list of attributes.
     */
    public List<AbstractAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Get the list of instances.
     *
     * @return The list of instances.
     */
    public List<Instance> getInstances() {
        return instances;
    }

    /**
     * Get the amount of positive instances in the ARFF file, for the target.
     *
     * @return The amount of positive instances.
     */
    public int getP() {
        return P;
    }

    /**
     * Get the amount of negative instances in the ARFF file, for the target.
     *
     * @return The amount of negative instances.
     */
    public int getN() {
        return N;
    }

    /**
     * Get the target attribute.
     *
     * @return The target attribute.
     */
    public AbstractAttribute getTargetAttribute() {
        return targetAttribute;
    }
}
