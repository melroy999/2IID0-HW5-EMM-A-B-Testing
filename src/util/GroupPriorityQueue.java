package util;

import group.Group;

import java.util.TreeSet;

public class GroupPriorityQueue extends TreeSet<Group> {
    private final int maximumCapacity;
    private double worstValue;

    public GroupPriorityQueue(int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    /**
     * Add the group to the priority queue. This method is synchronized, as it is used during multithreading.
     *
     * @param group The group to add.
     * @return Whether the group has been added successfully or not.
     */
    @Override
    public synchronized boolean add(Group group) {
        boolean returnValue = super.add(group);
        //Check if we are exceeding the maximum capacity.
        if(this.size() > maximumCapacity) {
            //Remove the worst performing group.
            this.pollLast();

            //Set the new worst value.
            worstValue = this.last().getEvaluation();
        }
        return returnValue;
    }

    public double getWorstValue() {
        return worstValue;
    }
}
