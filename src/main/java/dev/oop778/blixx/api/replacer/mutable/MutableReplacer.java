package dev.oop778.blixx.api.replacer.mutable;

import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.ReplaceActionCaller;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import org.jetbrains.annotations.CheckReturnValue;

/**
 * The Replacer interface provides a mechanism for registering placeholders
 * and context builders that allow custom logic to dynamically replace data in text
 * based on various contexts.
 */
public interface MutableReplacer extends ReplaceActionCaller, PlaceholderHolder<MutableReplacer> {

    static MutableReplacer create() {
        return new MutableReplacerImpl();
    }

    @CheckReturnValue
    Replacer toImmutable();
}
