package dev.oop778.blixx.api.parser;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class NodeKey {
    private final int index;
    private final Object parserKey;

    @Override
    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof NodeKey other)) {
            return false;
        }

        return this.index == other.index && this.parserKey.hashCode() == other.parserKey.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.index, this.parserKey);
    }
}
