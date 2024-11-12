package dev.oop778.blixx.api.replacer.action;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.processor.ReplacerProcessor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ReplaceActionImpl implements ReplaceAction<Object> {
    private final Object input;
    private final PlaceholderHolder<?> holder;
    private final List<ReplacerProcessor<Object, ?>> preProcessors = new ArrayList<>();
    private final List<ReplacerProcessor<Object, ?>> postProcessors = new ArrayList<>();
    private PlaceholderContext context;

    @Override
    public ReplaceAction<Object> context(@NonNull PlaceholderContext context) {
        this.context = context;
        return this;
    }

    @Override
    public <OUT> ReplaceAction<OUT> preReplacing(@NonNull ReplacerProcessor<Object, OUT> processor) {
        this.preProcessors.add(processor);
        return (ReplaceAction<OUT>) this;
    }

    @Override
    public <OUT> ReplaceAction<OUT> postReplacing(@NonNull ReplacerProcessor<Object, OUT> processor) {
        this.postProcessors.add(processor);
        return (ReplaceAction<OUT>) this;
    }

    @Override
    public Object complete() {
        Object current = this.input;
        for (final ReplacerProcessor<Object, ?> processor : this.preProcessors) {
            current = processor.accept(current);
        }

        // Do replacement
        current = this.doReplacement(current);

        for (final ReplacerProcessor<Object, ?> processor : this.postProcessors) {
            current = processor.accept(current);
        }

        return current;
    }

    private Object doReplacement(Object current) {
        if (current instanceof BlixxComponent) {
            return ((BlixxComponent) current).replace(this.holder.getPlaceholders(), this.context);
        }

        throw new IllegalStateException(String.format("idk how to handle %s", current.getClass().getSimpleName()));
    }
}
