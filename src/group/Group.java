package group;

import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import com.sun.javafx.image.impl.IntArgb;
import search.quality.AbstractQualityMeasure;
import search.result.ConfusionMatrix;

import java.util.*;

public class Group implements Comparable<Group> {
    //The constraints used within the group.
    private final HashSet<Constraint> constraints;

    //The product of all primes within the group.
    private final long product;

    //The evaluation of this group.
    private double evaluation;

    //The confusion matrix.
    private ConfusionMatrix confusionMatrix;

    /**
     * Create an empty group.
     */
    public Group() {
        this.constraints = new HashSet<>();

        //Calculate the product of the constraints' primes.
        this.product = 1;
    }

    /**
     * Create a group of level-i+1 that uses the specified constraint, together with the seed group.
     *
     * @param constraint The constraint that the group uses.
     * @param group The group to extend.
     * @param extendedProduct The pre-calculated new product of the group.
     */
    private Group(Constraint constraint, Group group, long extendedProduct) {
        this.constraints = new HashSet<>(group.getConstraints());
        this.constraints.add(constraint);

        //Calculate the product of the constraints' primes.
        this.product = extendedProduct;
    }

    /**
     * The list of constraints used within this group.
     *
     * @return The set of constraints that define the group.
     */
    public HashSet<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * The product of all primes connected to the constraint.
     * This product should contain both the complete constraint's prime, and the comparison prime.
     *
     * @return For all constraints in {@code constraints}, take the product of the connected prime.
     */
    public long getProduct() {
        return product;
    }

    /**
     * Get the evaluation of this group.
     *
     * @return The evaluation of this group, which is 0 when not set.
     */
    public double getEvaluation() {
        return evaluation;
    }

    /**
     * Extend the specified group by the constraint. Also adds the group to the encountered group set.
     *
     * @param constraint The constraint to extend by.
     * @param encounteredGroups The products of the groups we have already encountered.
     * @return A new group containing the constraint if that addition is valid, {@code null} otherwise.
     */
    public Group extendGroupWith(Constraint constraint, HashSet<Long> encounteredGroups) {
        long extendedProduct = this.product * constraint.getProduct();

        //Check whether the extension would be valid or not.
        if(isValidExtension(constraint, encounteredGroups, extendedProduct)) {
            //Add the value to the encountered group set.
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
    private boolean isValidExtension(Constraint constraint, HashSet<Long> encounteredGroups, long extendedProduct) {
        //Return null if the group is invalid.
        // - Duplicate value, as having the same value with different comparators are not helpful.
        // - Duplicate comparator, as having two duplicate comparators will not provide improvements.
        // - No groups that have the same product.

        //If the group already contains the given constraint, we will have that product modulo constraint.prime is 0.
        return !(encounteredGroups.contains(extendedProduct) || this.product % constraint.getValuePrime() == 0 || this.product % constraint.getComparisonPrime() == 0);
    }

    /**
     * Get the indices of instances that satisfy this group.
     *
     * @return Intersection of all subsets.
     */
    public List<Integer> getIndicesSubset() {
        List<List<Integer>> lists = new ArrayList<>();

        //Get all the indice subset lists.
        for(Constraint constraint : constraints) {
            lists.add(constraint.getIndicesSubsetForValue(constraint));
        }

        //Sort the list on their size.
        Collections.sort(lists, new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> o1, List<Integer> o2) {
                return Integer.compare(o1.size(), o2.size());
            }
        });

        //Debug
        for(List<Integer> list : lists) {
            System.out.println(list.size());
        }

        //Start the retain all chain.
        List<Integer> result = new ArrayList<>(lists.get(0));
        for(int i = 1; i < lists.size(); i++) {
            result.retainAll(lists.get(i));
        }

        System.out.println("Size: " + result.size());

        return result;
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
        //Add a minus, as we want larger values first.
        return -Double.compare(this.getEvaluation(), o.getEvaluation());
    }

    /**
     * Get the confusion matrix used within this group.
     *
     * @return The confusion matrix used within this group.
     */
    public ConfusionMatrix getConfusionMatrix() {
        return confusionMatrix;
    }

    public ConfusionMatrix getConfusionMatrix(Dataset dataset) {
        //Get the intersection of all the indices lists.
        List<Integer> indices = getIndicesSubset();

        //The coverage is the size of the list.
        int coverage = indices.size();

        //The target attribute.
        AbstractAttribute targetAttribute = dataset.getTargetAttribute();

        //The constraint the target attribute uses.
        Constraint constraint = targetAttribute.getTargetConstraint();

        //Determine which indices are in the positive set.
        List<Integer> positives = targetAttribute.getIndicesSubsetForValue(constraint);

        //Only keep the positive values.
        indices.retainAll(positives);

        //The amount of positive instances is the size of the list that has only positives retained.
        int p = indices.size();

        //TODO see if we can also add unknown cases.
        return new ConfusionMatrix(p, coverage - p, 0, 0, dataset.getP(), dataset.getN());
    }

    public double evaluateQuality(AbstractQualityMeasure qualityMeasure, Dataset dataset) {
        //Get the confusion matrix.
        this.confusionMatrix = getConfusionMatrix(dataset);

        //Evaluate the confusion matrix with the quality measure.
        this.evaluation = qualityMeasure.evaluate(confusionMatrix.p, confusionMatrix.n, confusionMatrix.P, confusionMatrix.N);

        return evaluation;
    }
}
