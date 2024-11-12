package dev.oop778.blixx.api.replacer.immutable;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.ReplacerAcceptable;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

public interface Replacer extends ReplacerAcceptable, PlaceholderHolder<Replacer> {
    static Replacer create() {
        return create(null);
    }

    static Replacer create(@Nullable Blixx blixx) {
        return new ReplacerImpl(blixx);
    }

    static Replacer create(@Nullable Blixx blixx, @Nullable BlixxPlaceholder<?> ...placeholder) {
        return new ReplacerImpl(blixx, placeholder);
    }

    @CheckReturnValue
    MutableReplacer toMutable();
}
