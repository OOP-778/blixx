package dev.oop778.blixx.util;

import java.util.ArrayList;
import java.util.List;

public class ArrayCharacterQueue {
    private final char[] queue;
    private int index;
    private IntQueue marks;

    public ArrayCharacterQueue(char[] queue) {
        this.queue = queue;
        this.index = -1;
    }

    public ArrayCharacterQueue(String string) {
        this(string.toCharArray());
    }

    public void mark() {
        if (this.marks == null) {
            this.marks = new IntQueue();
        }

        this.marks.enqueue(this.index);
    }

    public void jumpToMark() {
        this.index = this.marks.dequeue();
    }

    public int unmark() {
        return this.marks.dequeue();
    }

    public boolean hasNext() {
        return this.index + 1 < this.queue.length;
    }

    public char current() {
        return this.queue[this.index];
    }

    public char peek() {
        return this.queue[this.index + 1];
    }

    public char next() {
        this.index++;
        return this.queue[this.index];
    }

    public char at(int index) {
        return this.queue[index];
    }

    public int currentIndex() {
        return this.index;
    }

    public String[] makeStringOfRangeSplitBy(int start, int end, char delimiter) {
        final List<String> list = new ArrayList<>(3);
        int segmentStart = start;

        for (int i = start; i <= end; i++) {
            if (this.queue[i] == delimiter) {
                if (segmentStart < i) {
                    list.add(new String(this.queue, segmentStart, i - segmentStart));
                }

                segmentStart = i + 1;
            }
        }

        if (segmentStart <= end) {
            list.add(new String(this.queue, segmentStart, end - segmentStart + 1));
        }

        return list.toArray(new String[0]);
    }

    public boolean hasPrevious() {
        return this.index - 1 >= 1;
    }

    public char previous() {
        return this.queue[this.index - 1];
    }

    public boolean isPreviousEscape() {
        return this.hasPrevious() && this.previous() == '\\';
    }

    public void jumpBack(int by) {
        this.index = Math.max(-1, this.index - by);
    }

    public void jumpAhead(int by) {
        this.index = Math.min(this.queue.length - 1, this.index + by);
    }

    public String nextWhilstMatches(StringCharPredicate predicate) {
        final StringBuilder builder = new StringBuilder();
        while (this.hasNext() && predicate.test(builder.toString(), this.peek())) {
            builder.append(this.next());
        }

        return builder.toString();
    }

    public String nextWhilstNotMatches(StringCharPredicate predicate) {
        final StringBuilder builder = new StringBuilder();
        while (this.hasNext() && !predicate.test(builder.toString(), this.peek())) {
            builder.append(this.next());
        }

        return builder.toString();
    }

    public int findEnding(char endingChar, boolean checkEscape, boolean jumpBack) {
        final char startingChar = this.current();
        int depth = 1;

        this.mark();
        while (this.hasNext()) {
            final char current = this.next();
            if (current == startingChar) {
                depth++;
            } else if (current == endingChar) {
                if (--depth == 0 && (checkEscape && !this.isPreviousEscape())) {
                    this.unmark();
                    return this.index;
                }
            }
        }

        if (jumpBack) {
            this.jumpToMark();
        } else {
            this.unmark();
        }

        return -1;
    }

    public void jump(int parsingStart) {
        this.index = parsingStart;
    }

    @FunctionalInterface
    public interface StringCharPredicate {
        boolean test(String currentString, char current);
    }
}
