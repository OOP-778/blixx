package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.tag.BlixxTag;
import java.util.function.Predicate;

/** A node that holds parsed tags. Provides methods to query, add, replace, and filter tags. */
public interface BlixxTagHolder {
    boolean hasIndexableTagData();

    Iterable<BlixxTag.WithDefinedData<?>> getTags();

    boolean hasTag(BlixxTag.WithDefinedData<?> parsedTag);

    boolean hasTag(Predicate<BlixxTag.WithDefinedData<?>> tagFilterer);

    BlixxTag.WithDefinedData<?> findTag(Predicate<BlixxTag.WithDefinedData<?>> tagFilterer);

    void setTags(Iterable<BlixxTag.WithDefinedData<?>> tags);

    void replaceOrAddTag(BlixxTag.WithDefinedData<?> tag);

    boolean hasTags();
}
