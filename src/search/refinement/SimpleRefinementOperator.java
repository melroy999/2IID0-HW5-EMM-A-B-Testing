package search.refinement;

import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import group.Group;
import search.quality.AbstractQualityMeasure;

import java.util.HashSet;
import java.util.Set;

public class SimpleRefinementOperator extends AbstractRefinementOperator {
    @Override
    public Set<Group> generate(Group seed, Dataset dataset, AbstractQualityMeasure qualityMeasure, HashSet<Long> encounteredGroups, HashSet<String> blacklist, double minimumQuality) {
        HashSet<Group> groups = new HashSet<>();
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
