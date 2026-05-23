package dev.oop778.blixx.api.platform;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNode;
import org.jetbrains.annotations.Nullable;

public interface BlixxPlatform {
    @Nullable
    Object prebuild(Blixx blixx, BlixxTextNode node);

    Object build(BlixxComponent root);
}
