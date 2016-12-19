package search.refinement;

import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import group.Group;
import search.quality.AbstractQualityMeasure;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * A refinement operator that sorts the resulting set of groups on their quality, from high to low.
 */
public class QualityRefinementOperator extends AbstractRefinementOperator {
    /**
     * Generate a set of groups based upon the input seed.
     *
     * @param seed The group to use as a seed.
     * @param dataset The dataset to take the data from.
     * @param qualityMeasure The quality measure that is used.
     * @param encounteredGroups The prime products of the groups that have already been encountered.
     * @return A set of groups that can be used in the beam search.
     */
    @Override
    public Set<Group> generate(Group seed, Dataset dataset, AbstractQualityMeasure qualityMeasure, HashSet<BigInteger> encounteredGroups) {
        HashSet<Group> groups = new HashSet<>();
        //Extend the seed by attributes and constraints that are not similar.

        //Iterate over all attributes.
        for(AbstractAttribute attribute : dataset.getAttributes()) {
            if(dataset.getTargetAttribute() == attribute) {
                //Skip if the name is in the blacklist.
                continue;
            }

            //Iterate over all constraints.
            for(Object o : attribute.getConstraints()) {
                Constraint constraint = (Constraint) o;

                //Check if the quality of the constraint is sufficient.
                double quality = attribute.getConstraintEvaluation(constraint);

                if(quality < qualityMeasure.getMinimumValue()) {
                    continue;
                }

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

    /**
     * Get the name of the quality refinement mode.
     *
     * @return Full name of the quality refinement mode.
     */
    @Override
    public String getName() {
        return "Quality Refinement";
    }
}
