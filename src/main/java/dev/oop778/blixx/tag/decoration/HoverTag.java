package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.component.BlixxHoverEvent;
import dev.oop778.blixx.api.component.BlixxStyle;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.kind.BlixxPlaceholderNode;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.StringQueue;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class HoverTag implements BlixxTag<HoverTag.Action<?>> {
    public static final HoverTag INSTANCE = new HoverTag();
    public static final Processor PROCESSOR = new Processor();

    @Override
    public Action<?> createData(@NonNull BlixxProcessor.ParserContext context, @NotNull StringQueue args) {
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

    private Action<?> parseShowText(@NotNull StringQueue args, @NonNull BlixxProcessor.ParserContext context) {
        String text = args.pop();
        if (text.startsWith("\"") || text.startsWith("'")) {
            text = text.substring(1);
        }

        if (text.endsWith("\"") || text.endsWith("'")) {
            text = text.substring(0, text.length() - 1);
        }

        final BlixxNode parse = context.getBlixx().parseNode(text, null);
        return new ShowText(parse);
    }

    @RequiredArgsConstructor
    @Getter
    public abstract static class Action<T> implements Indexable<Action<T>> {
        protected final T value;

        public abstract void apply(BlixxStyle style);
    }

    public static class ShowText extends Action<BlixxNode> implements Indexable.WithNodeContent<Action<BlixxNode>> {
        public ShowText(BlixxNode node) {
            super(node);
        }

        @Override
        public void apply(@NotNull BlixxStyle style) {
            BlixxNode actualNode = this.value instanceof BlixxPlaceholderNode
                    ? ((BlixxPlaceholderNode) this.value).getRootNodeReplacement()
                    : this.value;
            style.hoverEvent(BlixxHoverEvent.showText(actualNode == null ? this.value : actualNode));
        }

        @Override
        public ShowText copy() {
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
            data.apply(context.getStyle());
        }
    }
}
