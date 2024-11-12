package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.NonNull;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.CheckReturnValue;

import java.util.List;

public interface BlixxComponent extends ComponentLike {
    BlixxComponent replace(List<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context);
    BlixxComponent copy();

    @CheckReturnValue
    BlixxComponent append(@NonNull BlixxComponent ...component);
    BlixxComponent append(@NonNull Iterable<BlixxComponent> components);

    BlixxNode getNode();
}
