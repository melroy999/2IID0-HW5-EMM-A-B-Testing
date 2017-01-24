package arff;

import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import arff.instance.Instance;
import group.Comparison;
import group.Group;
import search.result.RegressionModelEvaluation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.FileLoader;

import java.util.*;

public class Dataset {
    //The list of attributes within the dataset file.
    private final List<AbstractAttribute> attributes;

    //The list of instances within the dataset file.
    private final List<Instance> instances;

    //The relation name.
    private final String relationName;

    //The y target for the regression model.
    private final AbstractAttribute yTarget;

    //The x targets for the regression model.
    private final AbstractAttribute[] xTargets;

    //The lines of the file. Used as a buffer.
    private static final List<String> lines = new ArrayList<>();

    /**
     * Create a dataset.
     *
     * @param attributes The list of attributes.
     * @param instances The list of instances.
     * @param relationName The name of the relation.
     * @param yTarget The numeric y target.
     * @param xTargets  The numeric x targets.
     */
    public Dataset(List<AbstractAttribute> attributes, List<Instance> instances, String relationName, int yTarget, int[] xTargets) {
        this.instances = instances;
        this.attributes = attributes;
        this.relationName = relationName;

        this.yTarget = attributes.get(yTarget);
        this.xTargets = new AbstractAttribute[xTargets.length];
        for(int i = 0; i < xTargets.length; i++) {
            this.xTargets[i] = attributes.get(xTargets[i]);
        }

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
     * Get the relation name.
     *
     * @return The relation name, as denoted with @relation name.
     */
    public String getRelationName() {
        return relationName;
    }

    /**
     * Get the y target attribute.
     *
     * @return The y target attribute object.
     */
    public AbstractAttribute getYTarget() {
        return yTarget;
    }

    /**
     * Get the x target attributes.
     *
     * @return The x target attributes object.
     */
    public AbstractAttribute[] getXTargets() {
        return xTargets;
    }

    /**
     * Read the given arff file, and convert it to an object.
     *
     * @param filePath The path to the file we want to load.
     * @param yTarget The numeric y target.
     * @param xTargets  The numeric x targets.
     * @param countNullAsZero Whether we count null values as zero in numerical cases.
     * @param blacklist The blacklisted attributes.
     * @return The arff file as an object.
     * @throws Exception Throws an exception if the file cannot be loaded.
     */
    public static Dataset loadARFF(String filePath, String yTarget, String[] xTargets, boolean countNullAsZero, HashSet<String> blacklist) throws Exception {
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

        int yTargetId = findTargetAttributeId(yTarget, attributes);

        int[] xTargetIds = new int[xTargets.length];
        for(int i = 0; i < xTargets.length; i++) {
            xTargetIds[i] = findTargetAttributeId(xTargets[i], attributes);
        }

        return new Dataset(attributes, instances, relation, yTargetId, xTargetIds);
    }

    /**
     * Find the id associated to the given target attribute.
     *
     * @param targetAttribute The attribute name we want the id for.
     * @param attributes The list of all attributes.
     * @return The id of the attribute in question, throws an illegal argument exception otherwise.
     */
    private static int findTargetAttributeId(String targetAttribute, List<AbstractAttribute> attributes) {
        for (AbstractAttribute attribute : attributes) {
            //Find a match to the target attribute.
            if (attribute.getName().equals(targetAttribute)) {
                return attribute.getId();
            }
        }
        throw new IllegalArgumentException(">>> WARNING: target attribute [" + targetAttribute + "] could not be found.");
    }

    /**
     * Evaluate the subgroup that is denoted by the given indices.
     * @param indices The indices that are part of the subgroup.
     * @return An evaluation value according to cook's distance, together with the estimator vector.
     */
    public RegressionModelEvaluation getIndicesEvaluation(Set<Integer> indices) {
        throw new NotImplementedException();
        //return 0;
    }
}
