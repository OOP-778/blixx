package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
public class IndexedPlaceholder {
    private final List<Entry> entries;

    public IndexedPlaceholder(int length) {
        this.entries = new ArrayList<>(length);
    }

    public IndexedPlaceholder(List<Entry> entries) {
        this.entries = entries;
    }

    public IndexedPlaceholder() {
        this(2);
    }

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public void addIndexable(String fullPlaceholder, Indexable indexable) {
        final char start = fullPlaceholder.charAt(0);
        final char end = fullPlaceholder.charAt(fullPlaceholder.length() - 1);
        this.entries.add(new Entry(start, end, indexable));
    }

    @RequiredArgsConstructor
    @Getter
    public static class Entry {
        private final char start;
        private final char end;
        private final Indexable indexable;

        public String compileReplacement(String placeholder) {
            return this.start +
                    placeholder +
                    this.end;
        }
    }
}
