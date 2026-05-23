package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxTagHolder;
import dev.oop778.blixx.api.tag.BlixxTag;

public interface ComponentDecoration {
    static ComponentDecoration of(String input) {
        return (blixx) -> {
            final BlixxComponent parse = blixx.parseComponent(input);
            final BlixxNode node = parse.getNode();
            if (node instanceof BlixxTagHolder) {
                return ((BlixxTagHolder) node).getTags();
            }
            return null;
        };
    }

    static ComponentDecoration of(Iterable<? extends BlixxTag.WithDefinedData<?>> tags) {
        return ($) -> tags;
    }

    Iterable<? extends BlixxTag.WithDefinedData<?>> getTags(Blixx context);
}
