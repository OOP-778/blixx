package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import net.kyori.adventure.text.ComponentLike;

import java.util.List;

public interface BlixxComponent extends ComponentLike {
    BlixxComponent toMutable();

    BlixxComponent replace(List<BlixxPlaceholder<?>> placeholders, PlaceholderContext context);

    BlixxComponent copy();
}
