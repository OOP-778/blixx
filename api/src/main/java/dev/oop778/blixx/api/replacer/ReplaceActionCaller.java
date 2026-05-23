package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.replacer.action.ReplaceAction;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import org.jetbrains.annotations.NotNull;

/**
 * Starts a replacement operation on an object. Implemented by both {@link Replacer} and
 * {@link MutableReplacer} — kept separate from {@link PlaceholderHolder} so that callers
 * can trigger replacements without access to mutability-specific methods.
 */
public interface ReplaceActionCaller {
    /** Begins a replacement on the given object, returning a chainable {@link ReplaceAction}. */
    <T> ReplaceAction<T> accept(@NotNull T object);
}
