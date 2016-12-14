package util;

import group.Group;

import java.util.TreeSet;

public class GroupPriorityQueue extends TreeSet<Group> {
    private final int maximumCapacity;
    private double worstValue;

    public GroupPriorityQueue(int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    @Override
    public boolean add(Group group) {
        boolean returnValue = super.add(group);
        //Check if we are exceeding the maximum capacity.
        if(this.size() == maximumCapacity) {
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
