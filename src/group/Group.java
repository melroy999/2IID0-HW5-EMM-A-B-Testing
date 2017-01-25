package group;

import arff.Dataset;
import arff.attribute.Constraint;
import search.result.RegressionModelEvaluation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigInteger;
import java.util.*;

public class Group implements Comparable<Group> {
    //The constraints used within the group.
    private final LinkedList<Constraint> constraints;

    //The product of all primes within the group.
    private final BigInteger product;

    //The evaluation value of this group.
    private RegressionModelEvaluation evaluation;

    //The amount of instances in this group.
    private int coverage;

    /**
     * Create an empty group.
     */
    public Group() {
        this.constraints = new LinkedList<>();

        //Calculate the product of the constraints' primes.
        this.product = BigInteger.valueOf(1);
    }

    /**
     * Create a group of level-i+1 that uses the specified constraint, together with the seed group.
     *
     * @param constraint The constraint that the group uses.
     * @param group The group to extend.
     * @param extendedProduct The pre-calculated new product of the group.
     */
    private Group(Constraint constraint, Group group, BigInteger extendedProduct) {
        this.constraints = new LinkedList<>(group.getConstraints());
        this.constraints.add(constraint);

        //Calculate the product of the constraints' primes.
        this.product = extendedProduct;
    }

    /**
     * The list of constraints used within this group.
     *
     * @return The set of constraints that define the group.
     */
    public LinkedList<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * The product of all primes connected to the constraint.
     * This product should contain both the complete constraint's prime, and the comparison prime.
     *
     * @return For all constraints in {@code constraints}, take the product of the connected prime.
     */
    public BigInteger getProduct() {
        return product;
    }

    /**
     * Extend the specified group by the constraint.
     *
     * @param constraint The constraint to extend by.
     * @param encounteredGroups The products of the groups we have already encountered.
     * @return A new group containing the constraint if that addition is valid, {@code null} otherwise.
     */
    public Group extendGroupWith(Constraint constraint, HashSet<BigInteger> encounteredGroups) {
        BigInteger extendedProduct = this.product.multiply(constraint.getProduct());

        //Check whether the extension would be valid or not.
        if(isValidExtension(constraint, encounteredGroups, extendedProduct)) {
            //Add the new product to the encountered groups list.
            encounteredGroups.add(extendedProduct);

            //If it is valid, we want to make the group.
            return new Group(constraint, this, extendedProduct);
        }

        //If it is invalid, return null.
        return null;
    }

    /**
     * Whether the extension is valid or not.
     *
     * @param constraint The constraint to extend by.
     * @param encounteredGroups The products of the groups we have already encountered.
     * @param extendedProduct The pre-calculated new product of the extended group.
     * @return True if the group itself will not have the same constraint twice, and the resulting group has not been seen yet.
     */
    private boolean isValidExtension(Constraint constraint, HashSet<BigInteger> encounteredGroups, BigInteger extendedProduct) {
        //Return null if the group is invalid.
        // - Duplicate value, as having the same value with different comparators are not helpful.
        // - Duplicate comparator, as having two duplicate comparators will not provide improvements.
        // - No groups that have the same product.

        //If the group already contains the given constraint, we will have that product modulo constraint.prime is 0.
        return !(
                encounteredGroups.contains(extendedProduct) ||
                        this.product.mod(BigInteger.valueOf(constraint.getValuePrime())).equals(BigInteger.ZERO) ||
                        this.product.mod(BigInteger.valueOf(constraint.getComparisonPrime())).equals(BigInteger.ZERO)
        );
    }

    /**
     * Get the indices of instances that satisfy this group.
     *
     * @return Intersection of all subsets.
     */
    public Set<Integer> getIndicesSubset() {
        //We want the subsets to be sets, as this has a lot better contains performance than lists.
        List<Set<Integer>> lists = new ArrayList<>();

        //Get all the indice subset lists.
        for(Constraint constraint : constraints) {
            lists.add(constraint.getIndicesSubsetForValue());
        }

        //Sort the list on their size.
        Collections.sort(lists, new Comparator<Set<Integer>>() {
            @Override
            public int compare(Set<Integer> o1, Set<Integer> o2) {
                return Integer.compare(o1.size(), o2.size());
            }
        });

        //Make sure we have data...
        if(lists.isEmpty()) {
            return null;
        }

        //Start the retain all chain.
        Set<Integer> result = new HashSet<>(lists.get(0));
        for(int i = 1; i < lists.size(); i++) {
            result.retainAll(lists.get(i));
        }

        return result;
    }

    /**
     * Get the evaluation value of this group.
     *
     * @return The evaluation value.
     */
    public double getEvaluation() {
        return evaluation.evaluationValue;
    }

    /**
     * Get the regression beta estimators of this group.
     *
     * @return The regression beta estimators.
     */
    public double[] getEstimators() {
        return evaluation.estimators;
    }

    /**
     * Evaluate the quality of this group.
     *
     * @param dataset The dataset to take the data from.
     * @return The evaluation value according to the quality measure.
     */
    public double evaluateQuality(Dataset dataset, Set<Integer> seedIndices, double minimumCoverage, double maximumCoverage) {
        //Last addition:
        Constraint newConstraint = constraints.peekLast();

        //Get the intersection of all the indices lists.
        //We ADD the new values to this list, instead of overwriting it, as we do not want to edit the original.
        //Add all the indices to a new instance, this way we will not edit the original lists.
        Set<Integer> indices = new HashSet<>();

        //Make sure that the passed list of indices is not null.
        if(seedIndices != null) {
            //We start with the seed indices, as these will be smaller than the individual attribute's value
            //in most of the cases. As addAll iterates over the original list, less iterations will be performed.
            indices.addAll(seedIndices);

            //Do the intersection.
            indices.retainAll(newConstraint.getIndicesSubsetForValue());
        } else {
            indices.addAll(newConstraint.getIndicesSubsetForValue());
        }

        //The coverage is the size of the list.
        coverage = indices.size();

        //We want to skip empty sets, as this gives errors. As an optimization, we now check here for minimum size and maximum size, instead of in the constraints clause.
        if(coverage < minimumCoverage || coverage > maximumCoverage) {
            return -Double.MAX_VALUE;
        }

        //Evaluate the list of indices, and save the result.
        evaluation = dataset.getIndicesEvaluation(indices);

        //We only care about the numeric result for now, so return it.
        return evaluation.evaluationValue;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Group o) {
        //Take the inverse, as we want higher values at the top.
        int eval = -Double.compare(this.getEvaluation(), o.getEvaluation());

        //If they are equal, we also have to consider the contents...
        if(eval == 0) {
            //Sort on the size, make smaller sizes more important than larger constraints.
            eval = Integer.compare(this.constraints.size(), o.constraints.size());

            //If this is also equal... we will sort on product instead.
            if(eval == 0) {
                //Compare the products, as they should be unique.
                return this.product.compareTo(o.getProduct());
            } else {
                return eval;
            }
        } else {
            return eval;
        }
    }

    /**
     * Get the string representation of a group.
     *
     * @return String representation of a group.
     */
    @Override
    public String toString() {
        return getReadableConstraints() +
                ", evaluation: " + evaluation;
    }

    /**
     * Convert the set of constraints to a human readable string.
     *
     * @return The toString value of each constraint in the constraints list, combined with ∧.
     */
    public String getReadableConstraints() {
        if(constraints.isEmpty()) {
            return "{empty group}";
        }

        String result = "";
        boolean isFirst = true;
        for(Constraint constraint : constraints) {
            if(isFirst) {
                result += constraint.toString();
                isFirst = false;
            } else {
                result += " && " + constraint.toString();
            }
        }
        return result;
    }

    public int getCoverage() {
        return coverage;
    }
}
