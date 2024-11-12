package dev.oop778.blixx.util.collection;

import java.util.NoSuchElementException;

public class IntQueue {
    private int[] data;
    private int head; // Index of the next element to dequeue
    private int tail; // Index where the next element will be enqueued
    private int size; // Number of elements in the queue

    public IntQueue(int initialCapacity) {
        this.data = new int[initialCapacity];
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    public IntQueue() {
        this(16);
    }

    /**
     * Adds an integer to the end of the queue
     */
    public void enqueue(int value) {
        if (this.size == this.data.length) {
            this.resize();
        }

        this.data[this.tail] = value;
        this.tail = (this.tail + 1) % this.data.length;
        this.size++;
    }

    /**
     * Removes and returns the integer at the front of the queue
     */
    public int dequeue() {
        if (this.size == 0) {
            throw new NoSuchElementException("Queue is empty");
        }

        final int value = this.data[this.head];
        this.head = (this.head + 1) % this.data.length;
        this.size--;

        return value;
    }

    /**
     * Returns the integer at the front without removing it
     */
    public int peek() {
        if (this.size == 0) {
            throw new NoSuchElementException("Queue is empty");
        }
        return this.data[this.head];
    }

    /**
     * Checks if the queue is empty
     */
    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * Doubles the capacity of the array when it's full
     */
    private void resize() {
        final int newCapacity = this.data.length * 2;
        final int[] newData = new int[newCapacity];

        for (int i = 0; i < this.size; i++) {
            newData[i] = this.data[(this.head + i) % this.data.length];
        }

        this.data = newData;
        this.head = 0;
        this.tail = this.size;
    }
}
