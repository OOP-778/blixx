package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.tag.BlixxTag;

public interface ComponentDecoration {
    static ComponentDecoration of(String input) {
        return (blixx) -> {
            final BlixxComponent parse = blixx.parse(input);
            return ((BlixxComponentImpl) parse).getNode().getTags();
        };
    }

    static ComponentDecoration of(Iterable<? extends BlixxTag.WithDefinedData<?>> tags) {
        return ($) -> tags;
    }

    Iterable<? extends BlixxTag.WithDefinedData<?>> getTags(Blixx context);
}
