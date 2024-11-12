package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface that both {@link ImmutableReplacer} and {@link Replacer} implement, this way you can pass anything, but you can't modify cause if immutable implemented Replacer
 * Then it would make so you can mistake it for a mutable one and not use the return value if you modify it.
 */
public interface ReplacerAcceptable {
    @CheckReturnValue
    <T> T accept(@NotNull T object, @Nullable PlaceholderContext context);
}
