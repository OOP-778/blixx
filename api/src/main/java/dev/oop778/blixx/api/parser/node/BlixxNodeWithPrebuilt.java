package dev.oop778.blixx.api.parser.node;

import org.jetbrains.annotations.Nullable;

/** A node that caches a prebuilt platform-specific object from {@link dev.oop778.blixx.api.platform.BlixxPlatform#prebuild}. */
public interface BlixxNodeWithPrebuilt {
    @Nullable
    Object getPreBuilt();

    void prebuild();
}
