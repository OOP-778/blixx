package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholderBuilder;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface PlaceholderHolder<T extends PlaceholderHolder<T>> {
    Iterable<? extends BlixxPlaceholder<?>> getPlaceholders();

    T withPlaceholder(@NonNull Where where, BlixxPlaceholder<?> placeholder);

    default <VALUE> BlixxPlaceholderBuilder.SelectorStage<T, VALUE> withPlaceholderAtStart() {
        return new BlixxPlaceholderBuilder.SelectorStageImpl<>((placeholder) -> this.withPlaceholder(Where.START, placeholder));
    }

    default <VALUE> BlixxPlaceholderBuilder.SelectorStage<T, VALUE> withPlaceholderAtEnd() {
        return new BlixxPlaceholderBuilder.SelectorStageImpl<>((placeholder) -> this.withPlaceholder(Where.END, placeholder));
    }

    enum Where {
        START,
        END
    }
}
