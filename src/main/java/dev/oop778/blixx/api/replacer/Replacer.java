package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholderBuilder;
import lombok.NonNull;
import org.jetbrains.annotations.CheckReturnValue;

/**
 * The Replacer interface provides a mechanism for registering placeholders
 * and context builders that allow custom logic to dynamically replace data in text
 * based on various contexts.
 */
public interface Replacer {
    /**
     * Registers a new placeholder to the Replacer.
     *
     * @param placeholder The placeholder to register.
     * @param <T>         The type of the placeholder value.
     * @return The current Replacer instance.
     */
    <T> Replacer addPlaceholder(@NonNull BlixxPlaceholder<T> placeholder);

    @CheckReturnValue
    ImmutableReplacer toImmutable();

    /**
     * Provides a fluent builder interface for creating a new placeholder.
     *
     * @return A new placeholder builder.
     */
    default <T> BlixxPlaceholderBuilder.SelectorStage<? extends Replacer, T> addPlaceholder() {
        return new BlixxPlaceholderBuilder.SelectorStageImpl<>((placeholder) -> this.addPlaceholder(placeholder));
    }
}
