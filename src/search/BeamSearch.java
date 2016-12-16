package search;

import arff.Dataset;
import arff.attribute.AbstractAttribute;
import arff.attribute.Constraint;
import group.Group;
import search.quality.AbstractQualityMeasure;
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
    private ExecutorService executor;
    private final double minimumCoverage;
    private final double maximumCoverageFraction;
    private final boolean multiThreading;

    public BeamSearch(double minimumCoverage, double maximumCoverageFraction, boolean multiThreading) {
        this.minimumCoverage = minimumCoverage;
        this.maximumCoverageFraction = maximumCoverageFraction;
        this.multiThreading = multiThreading;
    }

    public GroupPriorityQueue search(Dataset dataset, AbstractQualityMeasure qualityMeasure, AbstractRefinementOperator refinementOperator, int w, int d, int resultSetSize, HashSet<String> blacklist) throws InterruptedException {
        //Create the executor.
        executor = Executors.newFixedThreadPool(8);

        //Initialize all the attributes.
        for(AbstractAttribute attribute : dataset.getAttributes()) {
            attribute.initializeConfusionMatrices(dataset, qualityMeasure);
        }

        //Create a candidate queue, and add the empty seed as the first element.
        PriorityQueue<Group> candidateQueue = new PriorityQueue<>();
        candidateQueue.add(new Group());

        //The group priority queue is a tree set of fixed size.
        GroupPriorityQueue resultSet = new GroupPriorityQueue(resultSetSize);

        //A hashset that keeps the encountered group products.
        HashSet<BigInteger> encounteredGroups = new HashSet<>();

        //The target attribute.
        AbstractAttribute targetAttribute = dataset.getTargetAttribute();

        //The constraint the target attribute uses.
        Constraint constraint = dataset.getTargetConstraint();

        //Determine which indices are in the positive set.
        Set<Integer> positives = targetAttribute.getIndicesSubsetForValue(constraint);

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

                //The null seeds.
                Set<Integer> seedNullIndices = seed.getNullIndicesSubset();

                System.out.println("\tEvaluating seed " + seed);
                //System.out.println("\t\tConfusionTable: " + seed.getConfusionMatrix());

                //Get the candidate subgroups from the seed.
                Set<Group> groups = refinementOperator.generate(seed, dataset, qualityMeasure, encounteredGroups, blacklist);

                //Iterate over all the groups, do this with multithreading if desired.
                if(multiThreading) {
                    iterateOverGroupsThreaded(dataset, qualityMeasure, resultSet, positives, beam, seedIndices, seedNullIndices, groups);
                } else {
                    iterateOverGroups(dataset, qualityMeasure, resultSet, positives, beam, seedIndices, seedNullIndices, groups);
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
     * @param dataset
     * @param qualityMeasure
     * @param resultSet
     * @param positives
     * @param beam
     * @param seedIndices
     * @param seedNullIndices
     * @param groups
     */
    private void iterateOverGroups(Dataset dataset, AbstractQualityMeasure qualityMeasure, GroupPriorityQueue resultSet, Set<Integer> positives, GroupPriorityQueue beam, Set<Integer> seedIndices, Set<Integer> seedNullIndices, Set<Group> groups) {
        //Iterate over all these groups.
        for(Group group : groups) {
            //System.out.println(Util.getCurrentTimeStamp() + " Evaluating seed " + group);

            //Get the quality.
            double quality = group.evaluateQuality(qualityMeasure, dataset, seedIndices, seedNullIndices, positives);

            //If the group satisfies all constraints.
            if(quality > qualityMeasure.getMinimumValue() && group.getConfusionMatrix().getCoverage() > minimumCoverage && group.getConfusionMatrix().getCoverage() < maximumCoverageFraction * dataset.getInstances().size()) {
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
     * @param dataset
     * @param qualityMeasure
     * @param resultSet
     * @param positives
     * @param beam
     * @param seedIndices
     * @param seedNullIndices
     * @param groups
     */
    private void iterateOverGroupsThreaded(Dataset dataset, AbstractQualityMeasure qualityMeasure, GroupPriorityQueue resultSet, Set<Integer> positives, GroupPriorityQueue beam, Set<Integer> seedIndices, Set<Integer> seedNullIndices, Set<Group> groups) throws InterruptedException {
        //Create a collection of callables that have to be ran.
        Collection<Callable<Group>> callables = new ArrayList<>();

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
                    double quality = group.evaluateQuality(qualityMeasure, dataset, seedIndices, seedNullIndices, positives);

                    //If the group satisfies all constraints.
                    if(quality > qualityMeasure.getMinimumValue() && group.getConfusionMatrix().getCoverage() > minimumCoverage && group.getConfusionMatrix().getCoverage() < maximumCoverageFraction * dataset.getInstances().size()) {
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
