package arff;

import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import arff.attribute.NumericAttribute;
import arff.instance.Instance;
import group.Comparison;
import group.Group;
import search.result.RegressionModelEvaluation;
import util.FileLoader;
import util.linearalgebra.Matrix;
import util.linearalgebra.NoSquareException;
import util.linearalgebra.Vector;

import java.util.*;
import java.util.stream.IntStream;

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

    //The initial seed.
    private final Group seed;

    //The lines of the file. Used as a buffer.
    private static final List<String> lines = new ArrayList<>();

    //The degrees of freedom.
    private final int p;

    //Matrices and other stuff that we need for cook's distance, and should be constant.
    private final Matrix X_T;
    private final Matrix X_T_X;
    private final Vector Y;
    private final Vector beta_estimator;
    private final Vector e;
    private final double p_s_2;

    /**
     * Create a dataset.
     *
     * @param attributes The list of attributes.
     * @param instances The list of instances.
     * @param relationName The name of the relation.
     * @param yTarget The numeric y target.
     * @param xTargets  The numeric x targets.
     * @param seedAttributes  The attributes that are present within the seed.
     * @param seedComparisons  The attribute comparisons that are present within the seed.
     * @param seedTargets  The attribute values that are present within the seed.
     */
    public Dataset(List<AbstractAttribute> attributes, List<Instance> instances, String relationName, int yTarget, int[] xTargets, int[] seedAttributes, Comparison[] seedComparisons, String[] seedTargets) {
        this.instances = instances;
        this.attributes = attributes;
        this.relationName = relationName;

        //Find the y and x targets as attributes.
        this.yTarget = attributes.get(yTarget);
        if (!(this.yTarget instanceof NumericAttribute)) throw new IllegalArgumentException("The target attribute " + this.yTarget.getName() + " is not numeric!");
        this.xTargets = new AbstractAttribute[xTargets.length];
        for(int i = 0; i < xTargets.length; i++) {
            this.xTargets[i] = attributes.get(xTargets[i]);
            if (!(this.xTargets[i] instanceof NumericAttribute)) throw new IllegalArgumentException("The target attribute " + this.xTargets[i].getName() + " is not numeric!");
        }

        //Initialize all the attributes.
        for(AbstractAttribute attribute : attributes) {
            attribute.initialize(this);
        }

        //The degrees of freedom is the x targets amount + 1.
        p = xTargets.length + 1;

        //Create the seed group.
        Group seed = new Group();
        for(int i = 0; i < seedAttributes.length; i++) {
            AbstractAttribute attribute = attributes.get(seedAttributes[i]);
            Comparison comparison = seedComparisons[i];
            String targetValue = seedTargets[i];
            Constraint addition = attribute.getConstraint(attribute.getName() + " " + comparison + " " + attribute.convertValue(targetValue));

            if(addition == null) {
                throw new IllegalArgumentException("Target value " + targetValue + " or comparison mode " + comparison + " is invalid. The target attribute only supports the following comparisons: " + Arrays.toString(attribute.getComparisons()) + ".");
            }

            seed = seed.extendGroupWith(addition, new HashSet<>());
        }
        this.seed = seed;

        //Set the data needed for the evaluation process.
        int n = instances.size();
        List<Integer> indices = new ArrayList<>(n);
        IntStream.range(0,n).forEach(indices::add);

        //Calculate data for the full dataset, to be used during the Cook's distance evaluations.
        //This includes the X_T, X_T_X, Y, beta_estimator, the difference e and the p_s_2 divisor in the Cook's distance.
        Matrix X = getXMatrix(indices);
        X_T = X.getTransposeMatrix();
        X_T_X = X_T.multiply(X);
        Y = getYVector(indices);
        beta_estimator = getBetaEstimator(new HashSet<>(indices));
        e = Y.subtract(X.multiply(beta_estimator));

        double s_2 = 0;
        for(int i = 0; i < e.size(); i++) {
            s_2 += e.getValue(i) * e.getValue(i);
        }
        s_2 = s_2 / (n - p);

        this.p_s_2 = p * s_2;

        System.out.println("Full dataset has evaluation " + getCooksDistance(new HashSet<>(indices)));

        //Make sure that when the group is empty, we still have a list of indices.
        Set<Integer> seedIndices = seed.getIndicesSubset();
        if(seed.getConstraints().isEmpty()) {
            seedIndices = new HashSet<>(indices);
        }

        System.out.println("Seed group [" + this.seed.getReadableConstraints() + "] has evaluation " + getCooksDistance(seedIndices));
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
     * Get the seed group.
     *
     * @return The seed group.
     */
    public Group getSeed() {
        return seed;
    }

    /**
     * Read the given arff file, and convert it to an object.
     *
     * @param filePath The path to the file we want to load.
     * @param yTarget The numeric y target.
     * @param xTargets  The numeric x targets.
     * @param seedAttributes  The attributes that are present within the seed.
     * @param seedComparisons  The attribute comparisons that are present within the seed.
     * @param seedTargets  The attribute values that are present within the seed.
     * @param countNullAsZero Whether we count null values as zero in numerical cases.
     * @param blacklist The blacklisted attributes.
     * @return The arff file as an object.
     * @throws Exception Throws an exception if the file cannot be loaded.
     */
    public static Dataset loadARFF(String filePath, String yTarget, String[] xTargets, String[] seedAttributes, Comparison[] seedComparisons, String[] seedTargets, boolean countNullAsZero, HashSet<String> blacklist) throws Exception {
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
        int[] seedIds = new int[seedAttributes.length];
        for(int i = 0; i < seedAttributes.length; i++) {
            seedIds[i] = findTargetAttributeId(seedAttributes[i], attributes);
        }

        return new Dataset(attributes, instances, relation, yTargetId, xTargetIds, seedIds, seedComparisons, seedTargets);
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
        return getCooksDistance(indices);
    }

    //Loads of buffers, for memory management. This makes sure that memory won't be a large issue.
    private static List<Integer> indices_list;
    private static double[][] get_x_matrix_data_buffer;
    private static Matrix get_beta_estimator_X;
    private static Matrix get_beta_estimator_X_T;
    private static Matrix get_beta_estimator_X_T_X;
    private static Matrix get_beta_estimator_X_T_X_inverse;

    private static double[] get_y_vector_data_buffer;
    private static Vector get_beta_estimator_Y;
    private static Vector get_beta_estimator_X_T_Y;

    private static Vector subgroup_beta_estimator;
    private static Vector beta_difference_vector;

    /**
     * Calculate the cook's distance as specified in the assignment paper.
     *
     * @param indices The indices that are RETAINED, not removed!
     * @return An object containing all evaluation information.
     */
    public synchronized RegressionModelEvaluation getCooksDistance(Set<Integer> indices) {
        //Get the beta estimator for the subgroup.
        subgroup_beta_estimator = getBetaEstimator(indices);

        //Get the difference of the two beta vectors.
        beta_difference_vector = subgroup_beta_estimator.subtract(beta_estimator);

        //The top part of the cook's distance equation, divided by the bottom part which is already known as p_2_s.
        double evaluation = beta_difference_vector.dot(X_T_X.multiply(beta_difference_vector)) / p_s_2;

        //Return the evaluation + the beta estimator of the subgroup.
        return new RegressionModelEvaluation(evaluation, subgroup_beta_estimator.getValues(), indices.size());
    }

    /**
     * Get the beta estimator used by the cook's distance.
     *
     * @param indices The indices that are RETAINED, not removed!
     * @return The beta estimation vector.
     */
    public Vector getBetaEstimator(Set<Integer> indices) {
        //Convert the indices set to a list.
        indices_list = new ArrayList<>(indices);

        //Calculate the beta estimator as mentioned in section 2 of the report.
        get_beta_estimator_X = getXMatrix(indices_list);
        get_beta_estimator_X_T = get_beta_estimator_X.getTransposeMatrix();

        get_beta_estimator_X_T_X = get_beta_estimator_X_T.multiply(get_beta_estimator_X);
        try {
            get_beta_estimator_X_T_X_inverse = get_beta_estimator_X_T_X.getInverse();
        } catch (NoSquareException e) {
            throw new IllegalArgumentException("Inverse matrix has not been given a square matrix!");
        }

        get_beta_estimator_Y = getYVector(indices_list);
        get_beta_estimator_X_T_Y = get_beta_estimator_X_T.multiply(get_beta_estimator_Y);

        return get_beta_estimator_X_T_X_inverse.multiply(get_beta_estimator_X_T_Y);
    }

    /**
     * Get the X matrix corresponding to the indices and the xtargets.
     *
     * @param indices The indices that are RETAINED, not removed!
     * @return Matrix in which the first column is 1, attribute values in the others.
     */
    public Matrix getXMatrix(List<Integer> indices) {
        //NOTE: we want the transpose of the actual matrix, so we switch i and j.
        get_x_matrix_data_buffer = new double[indices.size()][p];

        //Fill the data array with the appropriate values.
        for(int i = 0; i < p; i++) {
            NumericAttribute attribute = null;
            if(i > 0) {
                attribute = (NumericAttribute) xTargets[i-1];
            }
            for(int j = 0; j < indices.size(); j++) {
                //Fill it with 1s if i == 0.
                //NOTE: we want the transpose of the actual matrix, so we switch i and j.
                if(i == 0) {
                    get_x_matrix_data_buffer[j][i] = 1;
                } else {
                    //The current instance we would look at.
                    Instance instance = instances.get(indices.get(j));

                    //Take the data from the appropriate attribute.
                    //NOTE: we want the transpose of the actual matrix, so we switch i and j.
                    get_x_matrix_data_buffer[j][i] = attribute.getValue(instance);
                }
            }
        }

        if(indices.isEmpty()) {
            System.out.println("EMPTY! WE GOT A PROBLEM!");
        }

        //Create a matrix from this data.
        return new Matrix(get_x_matrix_data_buffer);
    }

    /**
     * Get the values of the y-target represented as a vector.
     *
     * @param indices The indices that are RETAINED, not removed!
     * @return Vector containing the values of the y-target for the indices that are retained.
     */
    public Vector getYVector(List<Integer> indices) {
        get_y_vector_data_buffer = new double[indices.size()];

        NumericAttribute attribute = (NumericAttribute) yTarget;
        for(int j = 0; j < indices.size(); j++) {
            Instance instance = instances.get(indices.get(j));
            get_y_vector_data_buffer[j] = attribute.getValue(instance);
        }

        return new Vector(get_y_vector_data_buffer);
    }
}
