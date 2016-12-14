package search.refinement;

import arff.Dataset;
import group.Group;

import java.util.Set;

public abstract class AbstractRefinementOperator {
    public abstract Set<Group> generate(Group seed, Dataset dataset);
}
