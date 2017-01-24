package search.refinement;

import arff.Dataset;
import group.Group;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract representation of a refinement operator.
 */
public abstract class AbstractRefinementOperator {
    /**
     * Generate a set of groups based upon the input seed.
     *
     * @param seed The group to use as a seed.
     * @param dataset The dataset to take the data from.
     * @param encounteredGroups The prime products of the groups that have already been encountered.
     * @param minimumQuality The minimum quality the group should have.
     * @return A set of groups that can be used in the beam search.
     */
    public abstract Set<Group> generate(Group seed, Dataset dataset, HashSet<BigInteger> encounteredGroups, double minimumQuality);

    /**
     * Get the name of the quality refinement mode.
     *
     * @return Full name of the quality refinement mode.
     */
    public abstract String getName();
}
