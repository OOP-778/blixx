package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.component.BlixxDecoration;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.StringQueue;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class DecorationTag implements BlixxTag<BlixxDecoration> {
    public static final DecorationTag INSTANCE = new DecorationTag();
    public static final Processor PROCESSOR = new Processor();

    @Override
    public BlixxDecoration createData(
            @NonNull BlixxProcessor.@NonNull ParserContext context, @NotNull StringQueue args) {
        final String decoration = args.pop();
        return BlixxDecoration.of(decoration);
    }

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<BlixxDecoration> {
        @Override
        public void decorate(@NonNull ComponentContext context) {
            context.getStyle().decorate(context.getData());
        }
    }
}
