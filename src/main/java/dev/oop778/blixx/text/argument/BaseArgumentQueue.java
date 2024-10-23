package dev.oop778.blixx.text.argument;

import java.util.OptionalInt;

public class BaseArgumentQueue {
    private final String[] args;
    private int index = -1;
    private String currentArg;

    public BaseArgumentQueue(String[] args) {this.args = args;}

    public String pop() {
        return ((this.currentArg = this.args[++this.index]) == null ? "" : this.currentArg);
    }

    public String peek() {
        return this.args[this.index + 1];
    }

    public boolean hasNext() {
        return this.index + 1 < this.args.length;
    }

    public void reset() {
        this.index = -1;
    }

    public String current() {
        return this.currentArg;
    }

    public OptionalInt currentAsInt() {
        try {
            return OptionalInt.of(Integer.parseInt(this.currentArg));
        } catch (NumberFormatException throwable) {
            return OptionalInt.empty();
        }
    }

    public int size() {
        return this.args.length;
    }
}
