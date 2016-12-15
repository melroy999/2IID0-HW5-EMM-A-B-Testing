package search.refinement;

import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import group.Group;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class MiddleCoverageBasedRefinement extends AbstractRefinementOperator {
    @Override
    public Set<Group> generate(Group seed, Dataset dataset, HashSet<Long> encounteredGroups, HashSet<String> blacklist) {
        TreeSet<Group> groups = new TreeSet<>(new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                //Calculate absolute of the difference between the half dataset size and the coverage, and compare these values.
                return Double.compare(Math.abs(dataset.getInstances().size() - 2 * o1.getConfusionMatrix().getCoverage()), Math.abs(dataset.getInstances().size() - 2 * o2.getConfusionMatrix().getCoverage()));
            }
        });
        //Extend the seed by attributes and constraints that are not similar.

        //Iterate over all attributes.
        for(AbstractAttribute attribute : dataset.getAttributes()) {
            if(blacklist.contains(attribute.getName()) || dataset.getTargetAttribute() == attribute) {
                //Skip if the name is in the blacklist.
                continue;
            }

            //Iterate over all constraints.
            for(Object o : attribute.getConstraints()) {
                Constraint constraint = (Constraint) o;
                Group group = seed.extendGroupWith(constraint, encounteredGroups);

                //The group will be null if no better groups can be found.
                if(group != null) {
                    //Add it to the set of groups.
                    groups.add(group);
                }
            }
        }

        return groups;
    }
}
