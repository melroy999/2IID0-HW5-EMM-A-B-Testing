package group;

import arff.attribute.Constraint;

import java.util.HashSet;

public class Group {
    //The constraints used within the group.
    private final HashSet<Constraint> constraints;

    //The product of all primes within the group.
    private final long product;

    /**
     * Create a group of level-1 that uses the specified constraint.
     *
     * @param constraint The constraint that the group uses.
     */
    public Group(Constraint constraint) {
        this.constraints = new HashSet<>();
        this.constraints.add(constraint);

        //Calculate the product of the constraints' primes.
        this.product = constraint.getValuePrime() * constraint.getComparisonPrime();
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
     * Extend the specified group by the constraint.
     *
     * @param constraint The constraint to extend by.
     * @param encounteredGroups The products of the groups we have already encountered.
     * @return A new group containing the constraint if that addition is valid, {@code null} otherwise.
     */
    public Group extendGroupWith(Constraint constraint, HashSet<Long> encounteredGroups) {
        long extendedProduct = this.product * constraint.getValuePrime() * constraint.getComparisonPrime();

        //Check whether the extension would be valid or not.
        if(isValidExtension(constraint, encounteredGroups, extendedProduct)) {
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
    public boolean isValidExtension(Constraint constraint, HashSet<Long> encounteredGroups, long extendedProduct) {
        //Return null if the group is invalid.
        // - Duplicate value, as having the same value with different comparators are not helpful.
        // - Duplicate comparator, as having two duplicate comparators will not provide improvements.
        // - No groups that have the same product.

        //If the group already contains the given constraint, we will have that product modulo constraint.prime is 0.
        return !(encounteredGroups.contains(extendedProduct) || this.product % constraint.getValuePrime() == 0 || this.product % constraint.getComparisonPrime() == 0);
    }
}
