package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholderBuilder;
import dev.oop778.blixx.util.UnsafeCast;
import lombok.NonNull;
import org.jetbrains.annotations.CheckReturnValue;

public interface ImmutableReplacer extends Replacer {

    @Override
    @CheckReturnValue
    default <T> BlixxPlaceholderBuilder.SelectorStage<ImmutableReplacer, T> addPlaceholder() {
        return UnsafeCast.cast(Replacer.super.addPlaceholder());
    }

    @Override
    @CheckReturnValue
    <T> ImmutableReplacer addPlaceholder(@NonNull BlixxPlaceholder<T> placeholder);

    @CheckReturnValue
    Replacer toMutable();
}
