package dev.oop778.blixx.api.replacer.mutable;

import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.ReplaceActionCaller;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.spi.BlixxFactoryHolder;
import org.jetbrains.annotations.CheckReturnValue;

public interface MutableReplacer extends ReplaceActionCaller, PlaceholderHolder<MutableReplacer> {
    static MutableReplacer create() {
        return BlixxFactoryHolder.get().createMutableReplacer();
    }

    @CheckReturnValue
    Replacer toImmutable();
}
