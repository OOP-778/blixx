package dev.oop778.blixx.api.parser.node;

import org.jetbrains.annotations.Nullable;

public interface BlixxNodeWithPrebuilt {
    @Nullable
    Object getPreBuilt();

    void prebuild();
}
