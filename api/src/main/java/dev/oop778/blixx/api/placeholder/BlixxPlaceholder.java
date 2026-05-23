package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.spi.BlixxFactoryHolder;
import dev.oop778.blixx.util.UnsafeCast;
import java.util.Collection;
import lombok.NonNull;
import org.jetbrains.annotations.UnknownNullability;

public interface BlixxPlaceholder<T> {
    static <T> BlixxPlaceholderBuilder.SelectorStage<BlixxPlaceholder<T>, T> builder() {
        return BlixxFactoryHolder.get().createPlaceholderBuilder(UnsafeCast::cast);
    }

    static <T> BlixxPlaceholder<T> literal(
            @NonNull @org.intellij.lang.annotations.Pattern("[a-zA-Z_0-9.]+") String key, @NonNull T value) {
        return UnsafeCast.cast(BlixxPlaceholder.<T>builder()
                .literal()
                .withKey(key)
                .withValue(value)
                .build());
    }

    T get(@UnknownNullability PlaceholderContext context);

    interface Literal<T> extends BlixxPlaceholder<T> {
        String key();

        Collection<String> keys();
    }

    interface Pattern<T> extends BlixxPlaceholder<T> {
        java.util.regex.Pattern pattern();
    }

    interface Contextual<T> extends BlixxPlaceholder<T> {}

    interface ContextualLiteral<T> extends Contextual<T>, Literal<T> {}

    interface ContextualPattern<T> extends Contextual<T>, Pattern<T> {}
}
