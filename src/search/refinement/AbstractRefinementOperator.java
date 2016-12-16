package search.refinement;

import arff.Dataset;
import group.Group;
import search.quality.AbstractQualityMeasure;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractRefinementOperator {
    public abstract Set<Group> generate(Group seed, Dataset dataset, AbstractQualityMeasure qualityMeasure, HashSet<BigInteger> encounteredGroups, HashSet<String> blacklist);

    public abstract String getName();
}
