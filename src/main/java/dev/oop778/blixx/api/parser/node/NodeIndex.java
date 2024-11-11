package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.indexable.Indexable;

public interface NodeIndex {
    void indexPlaceholders(Indexable node, Blixx blixx);
    boolean hasPlaceholders(Indexable node, boolean checkForTags);
}
