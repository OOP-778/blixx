package dev.oop778.blixx.api.replacer.composed;

import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.ReplacerAcceptable;
import lombok.NonNull;

public interface ComposedReplacer extends ReplacerAcceptable, PlaceholderHolder<ComposedReplacer> {
    static ComposedReplacer create(@NonNull PlaceholderHolder<?>... placeholderHolder) {
        return new ComposedReplacerImpl(placeholderHolder);
    }
}
