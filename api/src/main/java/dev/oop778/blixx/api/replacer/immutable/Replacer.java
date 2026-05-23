package dev.oop778.blixx.api.replacer.immutable;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.ReplaceActionCaller;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import dev.oop778.blixx.api.spi.BlixxFactoryHolder;
import lombok.NonNull;
import org.jetbrains.annotations.CheckReturnValue;

public interface Replacer extends ReplaceActionCaller, PlaceholderHolder<Replacer> {
    static Replacer createImmutable() {
        return BlixxFactoryHolder.get().createImmutableReplacer();
    }

    static MutableReplacer createMutable() {
        return BlixxFactoryHolder.get().createMutableReplacer();
    }

    static Replacer createImmutable(@NonNull BlixxPlaceholder<?>... placeholder) {
        return BlixxFactoryHolder.get().createImmutableReplacer(placeholder);
    }

    static MutableReplacer createMutable(@NonNull BlixxPlaceholder<?>... placeholder) {
        return BlixxFactoryHolder.get().createMutableReplacer(placeholder);
    }

    @CheckReturnValue
    MutableReplacer toMutable();
}
