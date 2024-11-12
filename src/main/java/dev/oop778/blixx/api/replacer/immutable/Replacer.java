package dev.oop778.blixx.api.replacer.immutable;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.ReplaceActionCaller;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import lombok.NonNull;
import org.jetbrains.annotations.CheckReturnValue;

public interface Replacer extends ReplaceActionCaller, PlaceholderHolder<Replacer> {
    static Replacer create() {
        return new ReplacerImpl();
    }

    static Replacer create(@NonNull BlixxPlaceholder<?>... placeholder) {
        return new ReplacerImpl(placeholder);
    }

    @CheckReturnValue
    MutableReplacer toMutable();
}
