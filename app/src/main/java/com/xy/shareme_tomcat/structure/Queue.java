package com.xy.shareme_tomcat.structure;

import java.util.LinkedList;

public abstract class Queue extends LinkedList {

    public void enqueueFromFront(Object obj) {
        addFirst(obj);
        onEnqueue(obj, true);
    }

    public void enqueueFromRear(Object obj) {
        addLast(obj);
        onEnqueue(obj, false);
    }

    public Object dequeueFromFront() {
        onDequeue(getFirst());
        return removeFirst();
    }

    public Object dequeueFromRear() {
        onDequeue(getLast());
        return removeLast();
    }

    protected abstract void onEnqueue(Object obj, boolean isFromFront);
    protected abstract void onDequeue(Object obj);
    public abstract void destroy();

}
