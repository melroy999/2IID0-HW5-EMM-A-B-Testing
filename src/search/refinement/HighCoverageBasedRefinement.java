package search.refinement;

import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import group.Group;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class HighCoverageBasedRefinement extends AbstractRefinementOperator {
    @Override
    public Set<Group> generate(Group seed, Dataset dataset, HashSet<Long> encounteredGroups, HashSet<String> blacklist) {
        TreeSet<Group> groups = new TreeSet<>(new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                //From high to low coverage.
                return -Double.compare(o1.getConfusionMatrix().getCoverage(), o2.getConfusionMatrix().getCoverage());
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
