package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholderBuilder;
import lombok.NonNull;

import java.util.function.Supplier;

public interface PlaceholderHolder<T extends PlaceholderHolder<T>> {
    Iterable<? extends BlixxPlaceholder<?>> getPlaceholders();

    T withPlaceholder(@NonNull Where where, BlixxPlaceholder<?> placeholder);

    default T withLiteral(@NonNull @org.intellij.lang.annotations.Pattern("[a-zA-Z_0-9.]+") String key, @NonNull Object value) {
        return this.withPlaceholder(BlixxPlaceholder.literal(key, value));
    }

    default T withLiteral(@NonNull @org.intellij.lang.annotations.Pattern("[a-zA-Z_0-9.]+") String key, @NonNull Supplier<Object> valueSupplier) {
        return this.withPlaceholder(BlixxPlaceholder.builder().literal().withKey(key).withValue(valueSupplier.get()).build());
    }

    default T withPlaceholder(BlixxPlaceholder<?> placeholder) {
        return this.withPlaceholder(Where.END, placeholder);
    }

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
