package dev.oop778.blixx.api.parser.indexable;

import dev.oop778.blixx.api.parser.node.BlixxNode;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface Indexable<SELF> {
    SELF copy();

    interface WithStringContent<SELF> extends Indexable<SELF> {
        String getContent();

        void setContent(String content);
    }

    interface WithNodeContent<SELF> extends Indexable<SELF> {
        BlixxNode getNode();
    }
}
