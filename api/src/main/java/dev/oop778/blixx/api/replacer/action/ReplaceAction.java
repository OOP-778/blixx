package dev.oop778.blixx.api.replacer.action;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.replacer.processor.ReplacerProcessor;
import lombok.NonNull;

/**
 * An in-progress replacement operation. Chain configuration methods, then call {@link #complete()} to finalize.
 *
 * @param <T> the result type
 */
public interface ReplaceAction<T> {
    /** Adds runtime context for contextual placeholder resolution. */
    ReplaceAction<T> context(@NonNull PlaceholderContext context);

    /** Applies a processor to the input before placeholder replacement. */
    <OUT> ReplaceAction<OUT> preReplacing(@NonNull ReplacerProcessor<? super T, OUT> processor);

    /** Applies a processor to the result after placeholder replacement. */
    <OUT> ReplaceAction<OUT> postReplacing(@NonNull ReplacerProcessor<? super T, OUT> processor);

    /** Executes the replacement and returns the result. */
    T complete();
}
