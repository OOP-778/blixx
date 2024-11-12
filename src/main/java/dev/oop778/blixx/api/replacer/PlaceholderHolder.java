package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholderBuilder;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.util.UnsafeCast;
import lombok.NonNull;

public interface PlaceholderHolder<T extends PlaceholderHolder<T>> {
    Iterable<? extends BlixxPlaceholder<?>> getPlaceholders();

    T withPlaceholder(@NonNull Where where, BlixxPlaceholder<?> placeholder);

    default BlixxPlaceholderBuilder.SelectorStage<Replacer, T> withPlaceholderAtStart() {
        return new BlixxPlaceholderBuilder.SelectorStageImpl<>((placeholder) -> UnsafeCast.cast(this.withPlaceholder(Where.START, placeholder)));
    }

    default BlixxPlaceholderBuilder.SelectorStage<Replacer, T> withPlaceholderAtEnd() {
        return new BlixxPlaceholderBuilder.SelectorStageImpl<>((placeholder) -> UnsafeCast.cast(this.withPlaceholder(Where.END, placeholder)));
    }

    enum Where {
        START,
        END
    }
}
