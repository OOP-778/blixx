package dev.oop.blixx.paper.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CommandArgs {
    private final Queue<String> args;

    public CommandArgs(String[] args) {
        this.args = new LinkedList<>(List.of(args));
    }

    public String peekNext() {
        return this.args.peek();
    }

    public boolean nextEquals(String value) {
        if (this.peekNext().equalsIgnoreCase(value)) {
            this.args.poll();
            return true;
        }

        return false;
    }

    public String next() {
        return this.args.poll();
    }

    public int nextInt() {
        return Integer.parseInt(this.next());
    }

    public double nextDouble() {
        return Double.parseDouble(this.next());
    }

    public String nextRemaining(String delimiter) {
        final String result = String.join(delimiter, this.args);
        this.args.clear();

        return result;
    }

    public List<String> collectNextRemaining() {
        final List<String> result = new ArrayList<>(this.args);
        this.args.clear();

        return result;
    }

    public boolean hasNext() {
        return !this.args.isEmpty();
    }

    public float nextFloat() {
        return Float.parseFloat(this.next());
    }
}
