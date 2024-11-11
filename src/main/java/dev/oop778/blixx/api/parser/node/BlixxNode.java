package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.tag.BlixxTag;
import net.kyori.adventure.text.TextComponent;

public interface BlixxNode extends Indexable.WithNodeContent {
    BlixxNodeImpl copy();

    @Override
    default BlixxNode getNode() {
        return this;
    }

    Iterable<BlixxTag.WithDefinedData<?>> getTags();

    TextComponent build();

    boolean hasPlaceholders(boolean withTags);

    BlixxNodeSpec getSpec();

    BlixxNodeImpl getNext();

    String getContent();

    boolean isHasIndexableTagData();
}
