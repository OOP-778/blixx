package dev.oop778.blixx.api.replacer.processor;

import dev.oop778.blixx.api.Blixx;
import lombok.NonNull;

/** Transforms a replacement input or output (e.g., splitting by newlines, joining components). */
@FunctionalInterface
public interface ReplacerProcessor<IN, OUT> {
    @NonNull
    OUT accept(IN input);

    interface Context {
        Blixx getBlixx();
    }
}
