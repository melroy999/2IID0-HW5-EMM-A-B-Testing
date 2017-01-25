package search;

import arff.Dataset;
import arff.attribute.AbstractAttribute;
import group.Group;
import search.refinement.AbstractRefinementOperator;
import util.GroupPriorityQueue;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class performing beam search.
 */
public class BeamSearch {
    //Parameters used by the beamsearch.
    private ExecutorService executor;
    private final double minimumCoverage;
    private final double maximumCoverageFraction;
    private final boolean multiThreading;

    private final double minimumQuality = 0;

    /**
     * Create the beam search object, with the given parameters.
     *
     * @param minimumCoverage The minimum coverage valid subgroups should have.
     * @param maximumCoverageFraction The maximum coverage fraction of the subgroups.
     * @param multiThreading Whether we use multi threading or not.
     */
    public BeamSearch(double minimumCoverage, double maximumCoverageFraction, boolean multiThreading) {
        this.minimumCoverage = minimumCoverage;
        this.maximumCoverageFraction = maximumCoverageFraction;
        this.multiThreading = multiThreading;
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
        //Create the executor.
        executor = Executors.newFixedThreadPool(8);

        //Initialize all the attributes.
        for(AbstractAttribute attribute : dataset.getAttributes()) {
            attribute.initializeConstraintEvaluations(dataset);
        }

        //Create a candidate queue, and add the empty seed as the first element.
        PriorityQueue<Group> candidateQueue = new PriorityQueue<>();
        candidateQueue.add(new Group());

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
                //System.out.println("\t\tConfusionTable: " + seed.getConfusionMatrix());

                //Get the candidate subgroups from the seed.
                Set<Group> groups = refinementOperator.generate(seed, dataset, encounteredGroups, minimumQuality);

                //Iterate over all the groups, do this with multithreading if desired.
                if(multiThreading) {
                    iterateOverGroupsThreaded(dataset, resultSet, beam, seedIndices, groups);
                } else {
                    iterateOverGroups(dataset, resultSet, beam, seedIndices, groups);
                }
            }

            //Empty the beam, and enqueue the beam elements into the candidate queue.
            while(!beam.isEmpty()) {
                //Add the first element within the beam.
                candidateQueue.add(beam.pollFirst());
            }
        }

        //Shut down the executor.
        executor.shutdown();

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
            double quality = group.evaluateQuality(dataset, seedIndices, minimumCoverage, maximumCoverage);

            //If the group satisfies all constraints.
            if(quality > minimumQuality) {
                //Add it to the result set.
                resultSet.add(group);

                //Insert it into the beam.
                beam.add(group);
            }
        }
    }

    /**
     * Iterating over groups with multithreading.
     *
     * @param dataset The dataset to use.
     * @param resultSet The result priority queue.
     * @param beam The beam object to use during the search.
     * @param seedIndices The indices that are part of the seed group.
     * @param groups The set of groups to iterate over.
     */
    private void iterateOverGroupsThreaded(Dataset dataset, GroupPriorityQueue resultSet, GroupPriorityQueue beam, Set<Integer> seedIndices, Set<Group> groups) throws InterruptedException {
        //Create a collection of callables that have to be ran.
        Collection<Callable<Group>> callables = new ArrayList<>();

        double maximumCoverage = maximumCoverageFraction * dataset.getInstances().size();

        //Iterate over all these groups.
        for(Group group : groups) {
            callables.add(new Callable<Group>() {
                /**
                 * Computes a result, or throws an exception if unable to do so.
                 *
                 * @return computed result
                 * @throws Exception if unable to compute a result
                 */
                @Override
                public Group call() throws Exception {
                    //Get the quality.
                    //Get the quality.
                    double quality = group.evaluateQuality(dataset, seedIndices, minimumCoverage, maximumCoverage);

                    //If the group satisfies all constraints.
                    if(quality > minimumQuality) {
                        //Add it to the result set.
                        resultSet.add(group);

                        //Insert it into the beam.
                        beam.add(group);
                    }
                    return null;
                }
            });
        }

        //We have to keep in mind that this can throw an exception.
        //Invoke all these callables, and wait untill they are all finished.
        executor.invokeAll(callables);
    }
}
