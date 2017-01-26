package arff.attribute;

import arff.Dataset;
import arff.instance.Instance;
import group.Comparison;
import util.SieveOfAtkin;

import java.math.BigInteger;
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

    //The list of constraint quality results.
    private final HashMap<BigInteger, Double> constraintToEvaluation = new HashMap<>();

    //The list of unique values found.
    private final HashSet<T> values = new HashSet<>();

    //The list of constraints.
    private final ArrayList<Constraint<T>> constraints = new ArrayList<>();

    //Mapping from string representation to constraint value.
    private final HashMap<String, Constraint<T>> stringToConstraint = new HashMap<>();

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

    //Whether we want to count null values as zero.
    private boolean countNullAsZero;

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
     * Whether we count numerical nulls as the value zero.
     *
     * @return Whether we count numerical nulls as the value zero.
     */
    public boolean isCountNullAsZero() {
        return countNullAsZero;
    }

    /**
     * Set whether we count numerical nulls as the value zero.
     *
     * @param countNullAsZero Whether we count numerical nulls as the value zero.
     */
    public void setCountNullAsZero(boolean countNullAsZero) {
        this.countNullAsZero = countNullAsZero;
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
        for(Comparison comparison : new Comparison[]{Comparison.EQ, Comparison.NEQ, Comparison.GTEQ, Comparison.LTEQ}) {
            primeMap.put(comparison, SieveOfAtkin.getNextPrime());
        }

        //Create the constraints.
        for(T value : values) {
            //The prime for this specific value.
            long prime = SieveOfAtkin.getNextPrime();

            //For each comparison mode we know this attribute uses.
            for(Comparison comparison : value != null ? this.getComparisons() : new Comparison[]{Comparison.EQ, Comparison.NEQ}) {
                Constraint<T> constraint = new Constraint<>(value, comparison, this, primeMap.get(comparison), prime);
                constraints.add(constraint);
                stringToConstraint.put(constraint.toString(), constraint);
            }
        }
    }

    /**
     * Initializes the attribute information sources.
     *
     * @param dataset The dataset file.
     */
    public void initializeConstraintEvaluations(Dataset dataset) {
        //Create the constraints.
        for(Constraint<T> constraint : constraints) {
            //Add the score of the confusion matrix.
            constraintToEvaluation.put(constraint.getProduct(), getConstraintEvaluation(constraint, dataset));
        }
    }

    /**
     * Calculate the quality of the constraint.
     *
     * @param constraint The constraint that is used.
     * @param dataset The dataset file.
     * @return The quality of the constraint.
     */
    public double getConstraintEvaluation(Constraint<T> constraint, Dataset dataset) {
        //Get the indices that are within the constraint.
        Set<Integer> indices = constraint.getIndicesSubsetForValue();

        //Let the dataset evaluate the indices that are retained. We only care about the evaluation value.
        return dataset.getIndicesEvaluation(indices).evaluationValue;
    }

    /**
     * Get the indices corresponding to the null case.
     *
     * @return The indices to the null case, empty if null is not present within the set.
     */
    public Set<Integer> getNullIndices() {
        Constraint<T> nullConstraint = stringToConstraint.get(name + " = null");
        //No null cases, so return an empty list.
        if(nullConstraint == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>(getIndicesSubsetForValue(nullConstraint));
        }
    }

    /**
     * Get the subset of the indices list that are covered by the constraint.
     *
     * @param constraint The constraint used.
     * @return A subset containing the indices of all instances that are covered by the constraint.
     */
    public Set<Integer> getIndicesSubsetForValue(Constraint<T> constraint) {
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

        //Create the indices set.
        //We want the subsets to be sets, as these have a lot better contains performance than lists.
        Set<Integer> indices = new HashSet<>();

        //Based on the constrain comparison mode, we will do something with the indexStart and indexEnd.
        switch (comparison) {
            case EQ:
                //Check range between index start and index end.
                indices.addAll(sortedIndices.subList(indexStart, indexEnd));
                break;
            case NEQ:
                //Check range between 0 and index start - 1, and index end + 1 till the end of the array.
                if(indexStart - 1 < 0) {
                    //if we are violating the range check, create an empty array.
                    //indices = new ArrayList<>();
                } else {
                    indices.addAll(sortedIndices.subList(0, indexStart));
                }

                //Make certain that the indexEnd + 1 is within bounds.
                if(indexEnd <= size - 1) {
                    //Index end is already +1, so don't do it here!
                    indices.addAll(sortedIndices.subList(indexEnd, size));
                }
                break;
            case LTEQ:
                //Check the range from 0 to index end.
                indices.addAll(sortedIndices.subList(0, indexEnd));

                //When null has to be counted as 0.
                if(countNullAsZero && nullStartIndex != -1 && getType() == Type.NUMERIC) {
                    if(this instanceof NumericAttribute && ((NumericAttribute) this).contains((Constraint<Double>) constraint, 0.0)) {
                        indices.addAll(sortedIndices.subList(nullStartIndex, size));
                    }
                }
                break;
            case GTEQ:
                //Check the range from index start to list size.
                indices.addAll(sortedIndices.subList(indexStart, nullStartIndex == -1 ? size : nullStartIndex));

                //When null has to be counted as 0.
                if(countNullAsZero && nullStartIndex != -1 && getType() == Type.NUMERIC) {
                    if(this instanceof NumericAttribute && ((NumericAttribute) this).contains((Constraint<Double>) constraint, 0.0)) {
                        indices.addAll(sortedIndices.subList(nullStartIndex, size));
                    }
                }
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
     * Get the constraint based on its string version.
     *
     * @param name The string version of the constraint.
     * @return The constraint that is connected to the name, if it exist. null otherwise.
     */
    public Constraint<T> getConstraint(String name) {
        return stringToConstraint.get(name);
    }

    /**
     * Get the list of comparisons used by this attribute.
     *
     * @return The list of comparisons.
     */
    public abstract Comparison[] getComparisons();

    /**
     * Get the quality of the single constraint.
     *
     * @param constraint The constraint used.
     * @return The quality of the confusion matrix connected to the constraint.
     */
    public Double getConstraintEvaluation(Constraint<T> constraint) {
        return constraintToEvaluation.get(constraint.getProduct());
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
        return "@attribute " + getName() + " " + getType();
    }

    /**
     * Extract the attribute from the line, and create it.
     *
     * @param line The line to parse.
     * @param id The id to give the attribute.
     * @param countNullAsZero Whether we count null values as zero in numerical cases.
     * @return An attribute object corresponding to the given line.
     * @throws Exception Throws an exception if no attributes can be found in the given line.
     */
    public static AbstractAttribute getAttribute(String line, int id, boolean countNullAsZero) throws Exception {
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
                NumericAttribute attribute = new NumericAttribute(name, id);
                attribute.setCountNullAsZero(countNullAsZero);
                return attribute;
            case SET:
                return new SetAttribute(name, id, value);
            case UUID:
                return new UUIDAttribute(name, id);
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
     * Get the prime number associated with this attribute.
     *
     * @return The prime number associated with this attribute.
     */
    public long getPrime() {
        return prime;
    }

    /**
     * Whether the value of the instance is contained by the constraint connected to this attribute.
     *
     * @param constraint The constraint to verify.
     * @param instance The instance to evaluate.
     * @return Whether the value in the instance connected to this attribute is contained within the constraint.
     */
    public abstract boolean contains(Constraint<T> constraint, Instance instance);

    /**
     * Whether the value is contained by the constraint connected to this attribute.
     *
     * @param constraint The constraint to verify.
     * @param value The value to evaluate.
     * @return Whether the value is contained within the constraint.
     */
    public abstract boolean contains(Constraint<T> constraint, T value);
}
