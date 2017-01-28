package search;

import arff.Dataset;
import arff.attribute.AbstractAttribute;
import group.Group;
import search.refinement.AbstractRefinementOperator;
import util.GroupPriorityQueue;

import java.math.BigInteger;
import java.util.*;

/**
 * Class performing beam search.
 */
public class BeamSearch {
    //Parameters used by the beamsearch.
    private final double minimumCoverage;
    private final double maximumCoverageFraction;
    private final double minimumQuality;

    /**
     * Create the beam search object, with the given parameters.
     *
     * @param minimumCoverage The minimum coverage valid subgroups should have.
     * @param maximumCoverageFraction The maximum coverage fraction of the subgroups.
     * @param minimumQuality The minimum quality the subgroups should have.
     */
    public BeamSearch(double minimumCoverage, double maximumCoverageFraction, double minimumQuality) {
        this.minimumCoverage = minimumCoverage;
        this.maximumCoverageFraction = maximumCoverageFraction;
        this.minimumQuality = minimumQuality;
    }

    /**
     * Do a beam search.
     *
     * @param dataset The dataset to use.
     * @param refinementOperator The refinement operator to use.
     * @param w The search width.
     * @param d The seardh depth.
     * @param resultSetSize The size of the result set, which corresponds to the value "q" in the algorithm.
     * @return A "Priority queue" of maximum size "resultSetSize" containing the best subgroups in order of evaluation value.
     * @throws InterruptedException When a thread is interrupted.
     */
    public GroupPriorityQueue search(Dataset dataset, AbstractRefinementOperator refinementOperator, int w, int d, int resultSetSize) throws InterruptedException {
        //Initialize all the attributes.
        for(AbstractAttribute attribute : dataset.getAttributes()) {
            attribute.initializeConstraintEvaluations(dataset);
        }

        //Create a candidate queue, and add the empty seed as the first element.
        PriorityQueue<Group> candidateQueue = new PriorityQueue<>();
        candidateQueue.add(dataset.getSeed());

        //The group priority queue is a tree set of fixed size.
        GroupPriorityQueue resultSet = new GroupPriorityQueue(resultSetSize);

        //A hashset that keeps the encountered group products.
        HashSet<BigInteger> encounteredGroups = new HashSet<>();

        //Iterate for all levels.
        for(int level = 1; level <= d; level++) {
            System.out.println("Entering level " + level);

            //Create the beam, which has a maximum amount of w entries.
            GroupPriorityQueue beam = new GroupPriorityQueue(w);

            //Evaluate all candidates in the candidate queue.
            while(!candidateQueue.isEmpty()) {
                //Take the head element from the queue.
                Group seed = candidateQueue.poll();

                //All groups are based on the seed, so we should save the information of the seed, and use this for further calculations.
                Set<Integer> seedIndices = seed.getIndicesSubset();

                System.out.println("\tEvaluating seed " + seed);

                //Get the candidate subgroups from the seed.
                Set<Group> groups = refinementOperator.generate(seed, dataset, encounteredGroups, minimumQuality);

                //Iterate over all the groups.
                iterateOverGroups(dataset, resultSet, beam, seedIndices, groups);
            }

            //Empty the beam, and enqueue the beam elements into the candidate queue.
            while(!beam.isEmpty()) {
                //Add the first element within the beam.
                candidateQueue.add(beam.pollFirst());
            }
        }
        return resultSet;
    }

    /**
     * A simple implementation of iterating over groups, without multithreading.
     *
     * @param dataset The dataset to use.
     * @param resultSet The result priority queue.
     * @param beam The beam object to use during the search.
     * @param seedIndices The indices that are part of the seed group.
     * @param groups The set of groups to iterate over.
     */
    private void iterateOverGroups(Dataset dataset, GroupPriorityQueue resultSet, GroupPriorityQueue beam, Set<Integer> seedIndices, Set<Group> groups) {
        double maximumCoverage = maximumCoverageFraction * dataset.getInstances().size();

        //Iterate over all these groups.
        for(Group group : groups) {
            //System.out.println(Util.getCurrentTimeStamp() + " Evaluating seed " + group);

            //Get the quality.
            double quality = group.evaluateQuality(dataset, seedIndices, minimumCoverage, maximumCoverage, seedIndices == null ? dataset.getInstances().size(): seedIndices.size());

            //If the group satisfies all constraints.
            if(quality > minimumQuality) {
                //Add it to the result set.
                resultSet.add(group);

                //Insert it into the beam.
                beam.add(group);
            }
        }
    }
}
