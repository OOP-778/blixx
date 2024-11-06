package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.tag.BlixxTag;
import net.kyori.adventure.text.TextComponent;

import java.util.List;

public interface BlixxNode extends Indexable.WithNodeContent {
    BlixxNodeImpl copy();

    dev.oop778.blixx.api.parser.indexable.IndexableKey getKey();

    @Override
    default BlixxNode getNode() {
        return this;
    }

    Iterable<BlixxTag.WithDefinedData<?>> getTags();

    TextComponent build();

    boolean hasPlaceholders();

    BlixxNodeSpec getSpec();

    BlixxNodeImpl getNext();

    String getContent();

    boolean isHasIndexableTagData();
}
