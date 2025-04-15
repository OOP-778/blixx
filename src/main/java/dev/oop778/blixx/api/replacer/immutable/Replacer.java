package dev.oop778.blixx.api.replacer.immutable;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.ReplaceActionCaller;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacerImpl;
import lombok.NonNull;
import org.jetbrains.annotations.CheckReturnValue;

public interface Replacer extends ReplaceActionCaller, PlaceholderHolder<Replacer> {
    static Replacer createImmutable() {
        return new ReplacerImpl();
    }

    static MutableReplacer createMutable() {
        return new MutableReplacerImpl();
    }

    static Replacer createImmutable(@NonNull BlixxPlaceholder<?>... placeholder) {
        return new ReplacerImpl(placeholder);
    }

    static MutableReplacer createMutable(@NonNull BlixxPlaceholder<?>... placeholder) {
        return new MutableReplacerImpl(placeholder);
    }

    @CheckReturnValue
    MutableReplacer toMutable();
}
