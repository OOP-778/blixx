package dev.oop778.blixx.api.replacer.mutable;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.ReplacerAcceptable;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

/**
 * The Replacer interface provides a mechanism for registering placeholders
 * and context builders that allow custom logic to dynamically replace data in text
 * based on various contexts.
 */
public interface MutableReplacer extends ReplacerAcceptable, PlaceholderHolder<MutableReplacer> {
    static MutableReplacer create() {
        return create(null);
    }

    static MutableReplacer create(@Nullable Blixx blixx) {
        return null;
    }

    @CheckReturnValue
    Replacer toImmutable();
}
