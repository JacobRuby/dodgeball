package dev.jacobruby.util;

import java.util.ArrayDeque;
import java.util.Collection;

public class RingQueue<T> extends ArrayDeque<T> {

    public RingQueue() {
    }

    public RingQueue(int numElements) {
        super(numElements);
    }

    public RingQueue(Collection<? extends T> c) {
        super(c);
    }

    @Override
    public T pollFirst() {
        T ret = super.pollFirst();
        addLast(ret);
        return ret;
    }

    @Override
    public T pollLast() {
        T ret = super.pollLast();
        addFirst(ret);
        return ret;
    }
}
