package arff;

import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import arff.instance.Instance;
import group.Comparison;
import group.Group;
import util.FileLoader;

import java.util.*;

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

    //Get the target group.
    private final Group targetGroup;

    //The target attribute.
    private final AbstractAttribute[] targetAttributes;

    /**
     * Create a dataset.
     *
     * @param attributes The list of attributes.
     * @param instances The list of instances.
     * @param relationName The name of the relation.
     * @param targetIds The ids of the target attributes.
     * @param targetValues The target values.
     * @param comparisons The comparisons used relative to the target values.
     */
    public Dataset(List<AbstractAttribute> attributes, List<Instance> instances, String relationName, int[] targetIds, String[] targetValues, Comparison[] comparisons) {
        this.instances = instances;
        this.attributes = attributes;
        this.relationName = relationName;

        this.targetAttributes = new AbstractAttribute[targetIds.length];
        for(int i = 0; i < targetIds.length; i++) {
            this.targetAttributes[i] = attributes.get(targetIds[i]);
        }

        //Initialize all the attributes.
        for(AbstractAttribute attribute : attributes) {
            attribute.initialize(this);
        }

        //Set the target group.
        Group targetGroup = new Group();
        for(int i = 0; i < this.targetAttributes.length; i++) {
            AbstractAttribute attribute = this.targetAttributes[i];
            Comparison comparison = comparisons[i];
            String targetValue = targetValues[i];
            Constraint addition = attribute.getConstraint(attribute.getName() + " " + comparison + " " + attribute.convertValue(targetValue));

            if(addition == null) {
                throw new IllegalArgumentException("Target value " + targetValue + " or comparison mode " + comparison + " is invalid. The target attribute only supports the following comparisons: " + Arrays.toString(attribute.getComparisons()) + ".");
            }

            targetGroup = targetGroup.extendGroupWith(addition, new HashSet<>());
        }

        //Set the target group.
        this.targetGroup = targetGroup;

        //Count the amount of positive instances.
        Set<Integer> indices = targetGroup.getIndicesSubset();

        //Set the P and N values.
        this.P = indices.size();
        this.N = instances.size() - indices.size();
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
     * Get the target attributes.
     *
     * @return The target attributes.
     */
    public AbstractAttribute[] getTargetAttributes() {
        return targetAttributes;
    }

    //The lines of the file.
    private static final List<String> lines = new ArrayList<>();

    /**
     * Read the given arff file, and convert it to an object.
     *
     * @param filePath The path to the file we want to load.
     * @param countNullAsZero Whether we count null values as zero in numerical cases.
     * @param targetAttributes Name of the target attributes.
     * @param targetValues The target values.
     * @param targetComparisons The comparisons to use relative to the target values.
     * @param blacklist The blacklisted attributes.
     * @return The arff file as an object.
     * @throws Exception Throws an exception if the file cannot be loaded.
     */
    public static Dataset loadARFF(String filePath, boolean countNullAsZero, String[] targetAttributes, String[] targetValues, Comparison[] targetComparisons, HashSet<String> blacklist) throws Exception {
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

        int[] targetAttributeIds = new int[targetAttributes.length];
        for(int i = 0; i < targetAttributes.length; i++) {
            String targetAttribute = targetAttributes[i];
            boolean targetAttributeFound = false;
            for (AbstractAttribute attribute : attributes) {
                //Find a match to the target attribute.
                if (attribute.getName().equals(targetAttribute)) {
                    targetAttributeFound = true;
                    targetAttributeIds[i] = attribute.getId();
                    break;
                }
            }

            if (!targetAttributeFound) {
                throw new IllegalArgumentException(">>> WARNING: target attribute [" + targetAttribute + "] could not be found.");
            }
        }

        System.out.println();

        return new Dataset(attributes, instances, relation, targetAttributeIds, targetValues, targetComparisons);
    }

    /**
     * Get the target constraint.
     *
     * @return The target constraint.
     */
    public Group getTargetGroup() {
        return targetGroup;
    }
}
