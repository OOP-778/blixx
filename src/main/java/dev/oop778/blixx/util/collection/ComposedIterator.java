package dev.oop778.blixx.util.collection;

import java.util.Collection;
import java.util.Iterator;

public class ComposedIterator<T> implements Iterator<T> {
    private final Iterator<? extends T>[] iterators;
    private int currentIteratorIndex;

    @SafeVarargs
    public ComposedIterator(Iterator<? extends T>... iterators) {
        this.iterators = iterators;
        this.currentIteratorIndex = 0;

        // Advance to the first non-empty iterator
        this.advance();
    }

    private void advance() {
        while (this.currentIteratorIndex < this.iterators.length && !this.iterators[this.currentIteratorIndex].hasNext()) {
            this.currentIteratorIndex++;
        }
    }

    public ComposedIterator(Iterable<? extends T>... iterables) {
        this.iterators = new Iterator[iterables.length];
        for (int i = 0; i < this.iterators.length; i++) {
            this.iterators[i] = iterables[i].iterator();
        }
    }

    public ComposedIterator(Collection<Iterator<T>> iterators) {
        this.iterators = new Iterator[iterators.size()];
        int i = 0;
        for (final Iterator<T> iterator : iterators) {
            this.iterators[i++] = iterator;
        }
    }

    @Override
    public boolean hasNext() {
        return this.currentIteratorIndex < this.iterators.length && this.iterators[this.currentIteratorIndex].hasNext();
    }

    @Override
    public T next() {
        final T nextElement = this.iterators[this.currentIteratorIndex].next();

        this.advance();
        return nextElement;
    }
}
