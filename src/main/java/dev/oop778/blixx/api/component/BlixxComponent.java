package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.NonNull;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

public interface BlixxComponent extends ComponentLike {
    BlixxComponent replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, @Nullable PlaceholderContext context);

    BlixxComponent copy();

    @CheckReturnValue
    BlixxComponent append(@NonNull BlixxComponent... component);

    BlixxComponent append(@NonNull Iterable<BlixxComponent> components);

    BlixxNode getNode();

    default BlixxComponent replace(Iterable<? extends BlixxPlaceholder<?>> placeholders) {
        return this.replace(placeholders, null);
    }
}
