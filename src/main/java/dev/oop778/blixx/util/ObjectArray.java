package dev.oop778.blixx.util;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ObjectArray<T> implements Iterable<T> {
    private T[] array;
    private int size;
    private int index;
    public static ObjectArray<?> EMPTY = new ObjectArray<>(0);

    @SuppressWarnings("unchecked")
    public ObjectArray(int size) {
        this.size = size;
        this.array = (T[]) new Object[size];
    }

    public ObjectArray(ObjectArray<T> array) {
        this.array = Arrays.copyOf(array.array, array.index);
        this.size = this.array.length;
        this.index = this.array.length;
    }

    public ObjectArray(T[] array) {
        this.array = array;
        this.size = this.array.length;
        this.index = this.array.length;
    }

    public ObjectArray(Stream<T> stream) {
        this.array = stream.toArray(size -> (T[]) new Object[size]);
        this.size = this.array.length;
        this.index = this.array.length;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new SafeIterator<>(new Iterator<T>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return this.currentIndex < ObjectArray.this.index;
            }

            @Override
            public T next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }

                return ObjectArray.this.array[this.currentIndex++];
            }
        });
    }

    @CheckReturnValue
    public ObjectArray<T> filter(Predicate<T> predicate) {
        final int size = this.size;
        final T[] copy = (T[]) new Object[size];

        int index = 0;
        for (int i = 0; i < size; i++) {
            final T object = this.array[i];
            if (object == null) {
                continue;
            }

            if (predicate.test(object)) {
                copy[index++] = object;
            }
        }

        // size down
        return new ObjectArray<>(index == size ? copy : Arrays.copyOf(copy, index));
    }

    @CheckReturnValue
    public <V> ObjectArray<V> map(Function<T, V> mapper) {
        final int size = this.size;
        final V[] copy = (V[]) new Object[size];

        for (int i = 0; i < size; i++) {
            final T object = this.array[i];
            if (object == null) {
                continue;
            }

            copy[i] = mapper.apply(this.array[i]);
        }

        return new ObjectArray<>(copy);
    }

    public void add(T object) {
        final int position = this.index++;
        if (position == this.size) {
            this.expand();
        }

        this.array[position] = object;
    }

    public boolean equals(ObjectArray<T> array) {
        final Iterator<T> iterator = this.iterator();
        final Iterator<T> otherIterator = array.iterator();

        while (iterator.hasNext()) {
            if (!otherIterator.hasNext()) {
                return false;
            }

            final T object = iterator.next();
            final T otherObject = otherIterator.next();

            if (!Objects.equals(object, otherObject)) {
                return false;
            }
        }

        return true;
    }

    public Stream<T> stream() {
        return Arrays.stream(this.array, 0, this.index).filter(Objects::nonNull);
    }

    public ObjectArray<T> copy() {
        return new ObjectArray<>(this);
    }

    private void expand() {
        this.array = Arrays.copyOf(this.array, this.array.length + 2);
        this.size = this.array.length;
    }
}
