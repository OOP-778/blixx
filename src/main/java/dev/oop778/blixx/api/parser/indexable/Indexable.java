package dev.oop778.blixx.api.parser.indexable;

import dev.oop778.blixx.api.parser.node.BlixxNode;

public interface Indexable {
    Object getKey();
    Indexable copy();

    interface WithStringContent extends Indexable {
        String getContent();
        void setContent(String content);
    }

    interface WithNodeContent extends Indexable {
        BlixxNode getNode();
    }
}
