package com.example.mytrace;

import java.util.LinkedList;

public class LimitedQueue<E> extends LinkedList<E> {
    private int limit;

    /**
     * Limited queue holds last {code limit} elements, remove oldest 
     * @param limit - maximum limit of the queue
     */
    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    /**
     * Add element to queue, if queue limit is reached then oldest element is removed
     */
    @Override
    public boolean add(E o) {
    	boolean added = super.add(o);
        while (added && (size() > limit)) { 
        	super.remove(); 
        }
        return added;
    }
}
