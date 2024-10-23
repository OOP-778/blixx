package dev.oop778.blixx.api.component;

import net.kyori.adventure.text.ComponentLike;

public interface BlixxComponent extends ComponentLike {
    BlixxComponent toMutable();
}
