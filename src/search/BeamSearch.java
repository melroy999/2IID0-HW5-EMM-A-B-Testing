package search;

import arff.Dataset;
import group.Group;
import search.quality.AbstractQualityMeasure;
import search.refinement.AbstractRefinementOperator;
import util.GroupPriorityQueue;

import java.util.PriorityQueue;
import java.util.Set;

/**
 * Class performing beam search.
 */
public class BeamSearch {
    public GroupPriorityQueue search(Dataset dataset, AbstractQualityMeasure qualityMeasure, AbstractRefinementOperator refinementOperator, int w, int d, int resultSetSize) {
        //Create a candidate queue, and add the empty seed as the first element.
        PriorityQueue<Group> candidateQueue = new PriorityQueue<>();
        candidateQueue.add(new Group());

        //The group priority queue is a tree set of fixed size.
        GroupPriorityQueue resultSet = new GroupPriorityQueue(resultSetSize);

        //Iterate for all levels.
        for(int level = 1; level <= d; level++) {
            //Create the beam, which has a maximum amount of w entries.
            GroupPriorityQueue beam = new GroupPriorityQueue(w);

            //Evaluate all candidates in the candidate queue.
            while(!candidateQueue.isEmpty()) {
                //Take the head element from the queue.
                Group seed = candidateQueue.poll();

                //Get the candidate subgroups from the seed.
                Set<Group> groups = refinementOperator.generate(seed, dataset);

                //Iterate over all these groups.
                for(Group group : groups) {
                    //Get the quality.
                    double quality = group.evaluateQuality(qualityMeasure, dataset);

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
