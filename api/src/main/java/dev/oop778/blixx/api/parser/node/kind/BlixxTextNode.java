package dev.oop778.blixx.api.parser.node.kind;

import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeWithPrebuilt;
import dev.oop778.blixx.api.parser.node.BlixxTagHolder;

public interface BlixxTextNode extends BlixxNode, BlixxTagHolder, BlixxNodeWithPrebuilt {
    String getContent();

    void setContent(String content);

    Object getPreBuilt();

    void setPreBuilt(Object built);
}
