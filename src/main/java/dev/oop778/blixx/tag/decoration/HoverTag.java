package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

public class HoverTag implements BlixxTag<HoverTag.Action<?>> {
    public static final HoverTag INSTANCE = new HoverTag();
    public static final Processor PROCESSOR = new Processor();

    @Override
    public Action<?> createData(@NonNull BlixxProcessor.ParserContext context, @NotNull BaseArgumentQueue args) {
        if (!args.hasNext()) {
            throw new IllegalStateException("Action not defined");
        }

        final String action = args.pop();
        if (action.equalsIgnoreCase("show_text")) {
            return this.parseShowText(args, context);
        }

        throw new IllegalStateException(String.format("Action %s not supported", action));
    }

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    private Action<?> parseShowText(@NotNull BaseArgumentQueue args, @NonNull BlixxProcessor.ParserContext context) {
        String text = args.pop();
        if (text.startsWith("\"")) {
            text = text.substring(1);
        }

        if (text.endsWith("\"")) {
            text = text.substring(0, text.length() - 1);
        }

        final BlixxNode parse = context.getBlixx().parseNode(text, null);
        return new ShowText(parse);
    }

    @RequiredArgsConstructor
    @Getter
    public abstract static class Action<T> implements Indexable {
        protected final T value;
        protected final Object key;

        public abstract void apply(Style.Builder builder);
    }

    public static class ShowText extends Action<BlixxNode> implements Indexable.WithNodeContent {
        public ShowText(BlixxNode node) {
            super(node, node.getKey());
        }

        @Override
        public void apply(@NotNull Style.Builder builder) {
            builder.hoverEvent(HoverEvent.showText(this.value.build()));
        }

        @Override
        public Indexable copy() {
            return new ShowText(this.value.copy());
        }

        @Override
        public BlixxNode getNode() {
            return this.value;
        }
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<HoverTag.Action<?>> {
        @Override
        public void decorate(@NotNull ComponentContext context) {
            final HoverTag.Action<?> data = context.getData();
            data.apply(context.getStyleBuilder());
        }
    }
}
