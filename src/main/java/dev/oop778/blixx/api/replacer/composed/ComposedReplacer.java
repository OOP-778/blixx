package dev.oop778.blixx.api.replacer.composed;

import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.ReplaceActionCaller;
import lombok.NonNull;

public interface ComposedReplacer extends ReplaceActionCaller, PlaceholderHolder<ComposedReplacer> {
    static ComposedReplacer create(@NonNull PlaceholderHolder<?>... placeholderHolder) {
        return new ComposedReplacerImpl(placeholderHolder);
    }
}
