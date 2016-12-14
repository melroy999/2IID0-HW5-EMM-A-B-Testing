package arff.attribute;

import arff.Dataset;
import arff.instance.Instance;
import group.Comparison;
import search.result.ConfusionMatrix;
import util.SieveOfAtkin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract representation of an attribute.
 */
public abstract class AbstractAttribute<T> {
    //Pattern used for attribute detection.
    private static final Pattern ATTRIBUTE_MATCH_PATTERN = Pattern.compile("@attribute\\s+([']?(?:[^']*)[']?)\\s+[{]?(.*?)[}]?");

    //The name of the attribute.
    private final String name;

    //The id of the attribute.
    private final int id;

    //The comparator used to find positive results.
    private Constraint<T> constraint;

    //The list of confusion matrices.
    private final HashMap<Long, ConfusionMatrix> constraintToConfusionMatrix = new HashMap<>();

    //The list of unique values found.
    private final HashSet<T> values = new HashSet<>();

    //The list of constraints.
    private final ArrayList<Constraint<T>> constraints = new ArrayList<>();

    //Sorted indices of the instances.
    private final ArrayList<Integer> sortedIndices = new ArrayList<>();

    //The starting point of the indices for the specified value.
    private final LinkedHashMap<T, Integer> valueIndicesStart = new LinkedHashMap<>();

    //The end point of the indices for the specified value.
    private final LinkedHashMap<T, Integer> valueIndicesEnd = new LinkedHashMap<>();

    //The highest index with actual values.
    private int nullStartIndex = -1;

    //The prime number associated with this attribute.
    private final long prime;

    //The size of the sample.
    private int size;

    /**
     * Create an attribute.
     *
     * @param name The name of the attribute.
     * @param id The id of the attribute.
     */
    public AbstractAttribute(String name, int id) {
        this.name = name;
        this.id = id;
        this.prime = SieveOfAtkin.getNextPrime();
    }

    /**
     * Initializes the attribute information sources.
     *
     * @param dataset The dataset file.
     */
    public void initialize(Dataset dataset) {
        //The list of instances.
        List<Instance> instances = new ArrayList<>(dataset.getInstances());

        //The size of the sample.
        this.size = instances.size();

        //Sort the list of instances based on the provided comparator.
        //Reverse the order, as we want nulls last instead of first!
        Collections.sort(instances, getComparator());

        //Remember the previous value.
        T previousValue = null;

        //Create a list of integer indices of the sorted instances collection.
        //Also save some useful information regarding value start and value end indices.
        int i;
        boolean foundNull = false;
        for(i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);
            sortedIndices.add(instance.getId());

            //Get the value.
            T value = getValue(instance);

            //if we added a value that was not known yet.
            if(values.add(value)) {
                if(previousValue != null) {
                    valueIndicesEnd.put(previousValue, i - 1);
                }

                //Set the new previous value.
                previousValue = value;

                if(value != null) {
                    valueIndicesStart.put(value, i);
                } else if(!foundNull) {
                    //Set the null start index, and break the loop.
                    nullStartIndex = i;
                    foundNull = true;
                }
            }
        }

        //Add the last value end index.
        if(previousValue != null) {
            valueIndicesEnd.put(previousValue, instances.size() - 1);
        }

        //Get the unique primes for the comparisons.
        HashMap<Comparison, Long> primeMap = new HashMap<>();
        for(Comparison comparison : getComparisons()) {
            primeMap.put(comparison, SieveOfAtkin.getNextPrime());
        }

        //Create the constraints.
        for(T value : values) {
            //The prime for this specific value.
            long prime = SieveOfAtkin.getNextPrime();

            //For each comparison mode we know this attribute uses.
            for(Comparison comparison : value != null ? getComparisons() : new Comparison[]{Comparison.EQ}) {
                Constraint<T> constraint = new Constraint<>(value, comparison, this, primeMap.get(comparison), prime);
                constraints.add(constraint);

                //Get the confusion matrix.
                ConfusionMatrix confusionMatrix = calculateConfusionMatrix(constraint, dataset);

                //Add the confusion matrix.
                addConfusionMatrix(constraint, confusionMatrix);
            }
        }
    }

    /**
     * Calculate the confusion matrix.
     *
     * @param constraint The constraint that is used.
     * @param dataset The dataset file.
     * @return The confusion matrix of the constraint on this attribute.
     */
    private ConfusionMatrix calculateConfusionMatrix(Constraint<T> constraint, Dataset dataset) {
        //The list of instances.
        List<Instance> instances = dataset.getInstances();

        //Initialize the counters used for the confusion matrix.
        double p = 0;
        double n = 0;
        double up = 0;
        double un = 0;

        //Get a subset of the indices set containing the values that are covered by the comparison and value combination.
        List<Integer> indices = getIndicesSubsetForValue(constraint);

        //The target attribute.
        AbstractAttribute targetAttribute = dataset.getTargetAttribute();

        //For each instance index.
        for(int index : indices) {
            Instance instance = instances.get(index);

            //If the value is positive increment, decrement otherwise.
            if(targetAttribute.matchesTargetValue(instance)) {
                p++;
            } else {
                n++;
            }
        }

        //Iterate over null cases, but only if the nullStartIndex is actually set.
        if(nullStartIndex != -1) {
            for(int i = nullStartIndex; i < instances.size(); i++) {
                Instance instance = instances.get(i);

                //If the value is positive increment, decrement otherwise.
                if(targetAttribute.matchesTargetValue(instance)) {
                    up++;
                } else {
                    un++;
                }
            }
        }

        //Create the confusion matrix.
        return new ConfusionMatrix(p, n, up, un, dataset.getP(), dataset.getN());
    }

    /**
     * Get the subset of the indices list that are covered by the constraint.
     *
     * @param constraint The constraint used.
     * @return A subset containing the indices of all instances that are covered by the constraint.
     */
    public List<Integer> getIndicesSubsetForValue(Constraint<T> constraint) {
        //Get the start and end indices.
        int indexStart;
        int indexEnd;

        //Get the value and comparison.
        T value = constraint.getValue();
        Comparison comparison = constraint.getComparison();

        //If value is null, we have to be at the end of the list...
        if(value == null) {
            //Get the start and end indices.
            indexStart = nullStartIndex;
            indexEnd = size;
        } else {
            //Get the start and end indices.
            indexStart = valueIndicesStart.get(value);
            indexEnd = valueIndicesEnd.get(value) + 1;
        }

        //Create the indices arraylist.
        List<Integer> indices = new ArrayList<>();

        //Based on the constrain comparison mode, we will do something with the indexStart and indexEnd.
        switch (comparison) {
            case EQ:
                //Check range between index start and index end.
                indices = sortedIndices.subList(indexStart, indexEnd);
                break;
            case NEQ:
                //Check range between 0 and index start - 1, and index end + 1 till the end of the array.
                if(indexStart - 1 < 0) {
                    //if we are violating the range check, create an empty array.
                    indices = new ArrayList<>();
                } else {
                    indices = new ArrayList<>(sortedIndices.subList(0, indexStart - 1));
                }

                //Make certain that the indexEnd + 1 is within bounds.
                if(indexEnd <= size - 1) {
                    //Keep in mind that subList outer limit is non-inclusive!
                    indices.addAll(sortedIndices.subList(indexEnd, size));
                }
                break;
            case LTEQ:
                //Check the range from 0 to index end.
                indices = sortedIndices.subList(0, indexEnd);
                break;
            case GTEQ:
                //Check the range from index start to list size.
                indices = sortedIndices.subList(indexStart, size);
                break;
        }
        return indices;
    }

    /**
     * Get the name of the attribute.
     *
     * @return The name of the attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the id of the attribute.
     *
     * @return The id of the attribute.
     */
    public int getId() {
        return id;
    }

    /**
     * Get the type of the attribute.
     *
     * @return The type of the attribute.
     */
    public abstract Type getType();

    /**
     * Get the value out of an instance for this particular attribute.
     *
     * @param instance The instance we want the value out of.
     * @return The value in the instance connected to this attribute.
     */
    public abstract T getValue(Instance instance);

    /**
     * Get all the values encountered for this attribute.
     *
     * @return A set of values.
     */
    public Set<T> getValues() {
        return values;
    }

    /**
     * Get the set of constraints.
     *
     * @return The list of constraints that this attribute can produce.
     */
    public ArrayList<Constraint<T>> getConstraints() {
        return constraints;
    }

    /**
     * Get the list of comparisons used by this attribute.
     *
     * @return The list of comparisons.
     */
    public abstract Comparison[] getComparisons();

    /**
     * Get the confusion matrix of the comparison and the instance.
     *
     * @param constraint The constraint used.
     * @return The confusion matrix connected to the value and the comparison.
     */
    public ConfusionMatrix getConfusionMatrix(Constraint<T> constraint) {
        return constraintToConfusionMatrix.get(constraint.getProduct());
    }

    /**
     * Add a confusion matrix to the collection of confusion matrices.
     *
     * @param constraint The constraint used.
     * @param confusionMatrix The confusion matrix itself.
     */
    private void addConfusionMatrix(Constraint<T> constraint, ConfusionMatrix confusionMatrix) {
        constraintToConfusionMatrix.put(constraint.getProduct(), confusionMatrix);
    }

    /**
     * Convert the value to the appropriate type.
     *
     * @param value The value we want to convert.
     * @return The value in the correct type.
     */
    public abstract T convertValue(String value);

    /**
     * Converts the attribute to its string representation.
     *
     * @return The name of the attribute, with @attribute as prefix.
     */
    @Override
    public String toString() {
        return "@attribute " + getName();
    }

    /**
     * Extract the attribute from the line, and create it.
     *
     * @param line The line to parse.
     * @param id The id to give the attribute.
     * @return An attribute object corresponding to the given line.
     * @throws Exception Throws an exception if no attributes can be found in the given line.
     */
    public static AbstractAttribute getAttribute(String line, int id) throws Exception {
        //Get the pattern matches.
        Matcher matches = ATTRIBUTE_MATCH_PATTERN.matcher(line);

        //Find matches.
        if(!matches.matches()) {
            throw new Exception("No attribute found.");
        }

        //Set the name, which is the first group in the regex.
        String name = matches.group(1);

        //The values are the second group in the regex.
        String value = matches.group(2);

        //Determine the Type.
        Type type = Type.getType(value);

        //Based on the type, create an actual instance.
        switch (type) {
            case BOOLEAN:
                return new BooleanAttribute(name, id);
            case NUMERIC:
                return new NumericAttribute(name, id);
            case SET:
                return new SetAttribute(name, id, value);
        }
        return null;
    }

    /**
     * Get the comparator the attribute uses.
     *
     * @return A comparator using the correct type of sorting, based on the values it contains.
     */
    public abstract Comparator<Instance> getComparator();

    /**
     * Get the target value.
     *
     * @return The target value.
     */
    public T getTargetValue() {
        return constraint.getValue();
    }

    /**
     * Whether the instance matches the target value.
     *
     * @param instance The instance that has to be checked.
     * @return Whether the instance target value matches the overall target value.
     */
    public abstract boolean matchesTargetValue(Instance instance);

    /**
     * Set the target constraint.
     *
     * @param value The value of the constraint.
     * @param comparison The comparison of the constraint.
     */
    public void setTargetConstraint(String value, Comparison comparison) {
        for(Constraint<T> constraint : getConstraints()) {
            if(constraint.getValue().equals(convertValue(value)) && constraint.getComparison() == comparison) {
                this.constraint = constraint;
            }
        }
        throw new IllegalArgumentException("No constraint exists for the value and comparison combination.");
    }

    /**
     * Get the target constraint.
     * @return The target constraint.
     */
    public Constraint<T> getTargetConstraint() {
        return constraint;
    }

    /**
     * Get the prime number associated with this attribute.
     *
     * @return The prime number associated with this attribute.
     */
    public long getPrime() {
        return prime;
    }
}
