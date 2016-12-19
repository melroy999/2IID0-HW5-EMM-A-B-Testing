package arff;

import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import arff.instance.Instance;
import group.Comparison;
import util.FileLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Dataset {
    //The list of attributes within the dataset file.
    private final List<AbstractAttribute> attributes;

    //The list of instances within the dataset file.
    private final List<Instance> instances;

    //The relation name.
    private final String relationName;

    //The amount of positive and negative cases within the total dataset.
    private final int P;
    private final int N;

    //Get the target constraint.
    private final Constraint targetConstraint;

    //The target attribute.
    private final AbstractAttribute targetAttribute;

    /**
     * Create a dataset.
     *
     * @param attributes The list of attributes.
     * @param instances The list of instances.
     * @param relationName The name of the relation.
     * @param targetId The id of the target attribute.
     * @param targetValue The target value.
     * @param comparison The comparison used relative to the target value.
     */
    public Dataset(List<AbstractAttribute> attributes, List<Instance> instances, String relationName, int targetId, String targetValue, Comparison comparison) {
        this.instances = instances;
        this.attributes = attributes;
        this.relationName = relationName;

        this.targetAttribute = attributes.get(targetId);

        //Initialize all the attributes.
        for(AbstractAttribute attribute : attributes) {
            attribute.initialize(this);
        }

        //Set the target constraint.
        this.targetConstraint = targetAttribute.getConstraint(targetAttribute.getName() + " " + comparison + " " + targetAttribute.convertValue(targetValue));

        if(targetConstraint == null) {
            throw new IllegalArgumentException("Target value " + targetValue + " or comparison mode " + comparison + " is invalid. The target attribute only supports the following comparisons: " + Arrays.toString(targetAttribute.getComparisons()) + ".");
        }

        //Intermediary counters.
        int P = 0;
        int N = 0;

        //Count the amount of positive and negative cases.
        for(Instance instance : instances) {
            if(targetAttribute.matchesTargetValue(instance, this)) {
                P++;
            } else {
                N++;
            }
        }

        //Set the P and N values.
        this.P = P;
        this.N = N;
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
     * Get the amount of positive instances in the Dataset file, for the target.
     *
     * @return The amount of positive instances.
     */
    public int getP() {
        return P;
    }

    /**
     * Get the amount of negative instances in the Dataset file, for the target.
     *
     * @return The amount of negative instances.
     */
    public int getN() {
        return N;
    }

    /**
     * Get the relation name.
     *
     * @return The relation name, as denoted with @relation name.
     */
    public String getRelationName() {
        return relationName;
    }

    /**
     * Get the target attribute.
     *
     * @return The target attribute.
     */
    public AbstractAttribute getTargetAttribute() {
        return targetAttribute;
    }

    //The lines of the file.
    private static final List<String> lines = new ArrayList<>();

    /**
     * Read the given arff file, and convert it to an object.
     *
     * @param filePath The path to the file we want to load.
     * @param countNullAsZero Whether we count null values as zero in numerical cases.
     * @param targetAttribute Name of the target attribute.
     * @param targetValue The target value.
     * @param targetComparison The comparison to use relative to the target value.
     * @param blacklist The blacklisted attributes.
     * @return The arff file as an object.
     * @throws Exception Throws an exception if the file cannot be loaded.
     */
    public static Dataset loadARFF(String filePath, boolean countNullAsZero, String targetAttribute, String targetValue, Comparison targetComparison, HashSet<String> blacklist) throws Exception {
        lines.clear();
        lines.addAll(FileLoader.readAllLines(filePath));

        List<AbstractAttribute> attributes = new ArrayList<>();
        String relation = "";
        List<Instance> instances = new ArrayList<>();
        List<Integer> offset = new ArrayList<>();

        int attributeCounter = 0;
        int instanceCounter = 0;
        int offsetValue = 0;
        for(String line : lines) {
            if(line.startsWith("@attribute")) {
                AbstractAttribute attribute = AbstractAttribute.getAttribute(line, attributeCounter, countNullAsZero);
                if(!blacklist.contains(attribute.getName())) {
                    attributes.add(attribute);
                    offset.add(offsetValue);
                    attributeCounter++;
                } else {
                    System.out.println("Skipped adding the attribute " + attribute.getName());
                    offsetValue++;
                }
            } else if(line.startsWith("@relation")) {
                relation = line.replaceFirst("@relation ","").replaceAll("'","");
            } else if(line.contains(",")) {
                instances.add(new Instance(instanceCounter++, line, attributes, blacklist, offset));
            }
        }

        //Set the temporary target attribute id.
        int targetAttributeId = attributes.size() - 1;
        boolean targetAttributeFound = false;
        for(AbstractAttribute attribute : attributes) {
            //Find a match to the target attribute.
            if (attribute.getName().equals(targetAttribute)) {
                targetAttributeFound = true;
                targetAttributeId = attribute.getId();
                break;
            }
        }

        //Warn for default behavior.
        if(!targetAttributeFound && !targetAttribute.equals("")) {
            System.out.println(">>> WARNING: target attribute [" + targetAttribute + "] could not be found, taking last attribute on default.");
        }

        return new Dataset(attributes, instances, relation, targetAttributeId, targetValue, targetComparison);
    }

    /**
     * Get the target constraint.
     *
     * @return The target constraint.
     */
    public Constraint getTargetConstraint() {
        return targetConstraint;
    }
}
