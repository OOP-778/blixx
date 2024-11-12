package dev.oop778.blixx.api.replacer.action;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.replacer.processor.ReplacerProcessor;
import lombok.NonNull;

public interface ReplaceAction<T> {
    ReplaceAction<T> context(@NonNull PlaceholderContext context);

    <OUT> ReplaceAction<OUT> preReplacing(@NonNull ReplacerProcessor<T, OUT> processor);
    <OUT> ReplaceAction<OUT> postReplacing(@NonNull ReplacerProcessor<T, OUT> processor);

    T complete();
}
