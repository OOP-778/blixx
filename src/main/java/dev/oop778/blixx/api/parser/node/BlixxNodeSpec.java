package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import org.jetbrains.annotations.UnknownNullability;

public interface BlixxNodeSpec extends NodeIndex {
    Blixx getBlixx();

    BlixxNode createNode();

    @UnknownNullability
    Object createNodeKey();

    default Indexable unwrap(Indexable indexable) {
        return indexable instanceof Indexable.WithNodeContent ? ((Indexable.WithNodeContent) indexable).getNode() : indexable;
    }
}
