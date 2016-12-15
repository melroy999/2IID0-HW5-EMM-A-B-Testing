package search;

import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import group.Group;
import search.quality.AbstractQualityMeasure;
import search.refinement.AbstractRefinementOperator;
import util.GroupPriorityQueue;
import util.Util;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Class performing beam search.
 */
public class BeamSearch {
    public GroupPriorityQueue search(Dataset dataset, AbstractQualityMeasure qualityMeasure, AbstractRefinementOperator refinementOperator, int w, int d, int resultSetSize, HashSet<String> blacklist) {
        //Create a candidate queue, and add the empty seed as the first element.
        PriorityQueue<Group> candidateQueue = new PriorityQueue<>();
        candidateQueue.add(new Group());

        //The group priority queue is a tree set of fixed size.
        GroupPriorityQueue resultSet = new GroupPriorityQueue(resultSetSize);

        //A hashset that keeps the encountered group products.
        HashSet<Long> encounteredGroups = new HashSet<>();

        //The target attribute.
        AbstractAttribute targetAttribute = dataset.getTargetAttribute();

        //The constraint the target attribute uses.
        Constraint constraint = dataset.getTargetConstraint();

        //Determine which indices are in the positive set.
        List<Integer> positives = targetAttribute.getIndicesSubsetForValue(constraint);

        //Iterate for all levels.
        for(int level = 1; level <= d; level++) {
            System.out.println(Util.getCurrentTimeStamp() + " Entering level " + level);

            //Create the beam, which has a maximum amount of w entries.
            GroupPriorityQueue beam = new GroupPriorityQueue(w);

            //Evaluate all candidates in the candidate queue.
            while(!candidateQueue.isEmpty()) {
                //Take the head element from the queue.
                Group seed = candidateQueue.poll();

                //All groups are based on the seed, so we should save the information of the seed, and use this for further calculations.
                List<Integer> seedIndices = seed.getIndicesSubset();

                //The null seeds.
                Set<Integer> seedNullIndices = seed.getNullIndicesSubset();

                System.out.println(Util.getCurrentTimeStamp() + " Evaluating seed " + seed);
                System.out.println("ConfusionTable: " + seed.getConfusionMatrix());

                //Get the candidate subgroups from the seed.
                Set<Group> groups = refinementOperator.generate(seed, dataset, encounteredGroups, blacklist);

                //Iterate over all these groups.
                for(Group group : groups) {
                    //System.out.println(Util.getCurrentTimeStamp() + " Evaluating seed " + group);

                    //Get the quality.
                    double quality = group.evaluateQuality(qualityMeasure, dataset, seedIndices, seedNullIndices, positives);

                    //If the group satisfies all constraints.
                    if(quality > 0) {
                        //Add it to the result set.
                        resultSet.add(group);

                        //Insert it into the beam.
                        beam.add(group);
                    }
                }
            }

            //Empty the beam, and enqueue the beam elements into the candidate queue.
            while(!beam.isEmpty()) {
                //Add the first element within the beam.
                candidateQueue.add(beam.pollFirst());
            }
        }

        return resultSet;
    }
}
