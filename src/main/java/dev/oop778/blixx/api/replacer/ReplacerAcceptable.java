package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.replacer.action.ReplaceAction;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface that both {@link Replacer} and {@link MutableReplacer} implement, this way you can pass anything, but you can't modify cause if immutable implemented Replacer
 * Then it would make so you can mistake it for a mutable one and not use the return value if you modify it.
 */
public interface ReplacerAcceptable {
    <T> ReplaceAction<T> accept(@NotNull T object);
}
