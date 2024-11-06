package dev.oop778.blixx.api.parser.indexable;

import java.util.Objects;

public class IndexableKey {
    private final int index;
    private final Object parserKey;
    private final int hashCode;

    public IndexableKey(int index, Object parserKey) {
        this.index = index;
        this.parserKey = parserKey;
        this.hashCode = Objects.hash(index, parserKey);
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof IndexableKey)) {
            return false;
        }

        final IndexableKey other = (IndexableKey) object;
        return this.index == other.index && this.parserKey == other.parserKey;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
