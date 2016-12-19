package search.refinement;

import arff.Dataset;
import group.Group;
import search.quality.AbstractQualityMeasure;

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
     * @param qualityMeasure The quality measure that is used.
     * @param encounteredGroups The prime products of the groups that have already been encountered.
     * @return A set of groups that can be used in the beam search.
     */
    public abstract Set<Group> generate(Group seed, Dataset dataset, AbstractQualityMeasure qualityMeasure, HashSet<BigInteger> encounteredGroups);

    /**
     * Get the name of the quality refinement mode.
     *
     * @return Full name of the quality refinement mode.
     */
    public abstract String getName();
}
