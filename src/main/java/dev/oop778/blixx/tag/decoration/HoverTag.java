package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

public class HoverTag implements BlixxTag<HoverTag.Action<?>> {
    public static final HoverTag INSTANCE = new HoverTag();
    public static final Processor PROCESSOR = new Processor();

    @Override
    public Action<?> createData(BlixxProcessor.@NonNull Context context, @NotNull BaseArgumentQueue args) {
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

    private Action<?> parseShowText(@NotNull BaseArgumentQueue args, BlixxProcessor.Context context) {
        final String text = args.pop();
        final BlixxComponent parse = context.getBlixx().parse(text);

        return new ShowText(parse);
    }

    @RequiredArgsConstructor
    public abstract static class Action<T> {
        protected final T value;

        public abstract void apply(Style.Builder builder);
    }

    public static class ShowText extends Action<BlixxComponent> {
        public ShowText(BlixxComponent value) {
            super(value);
        }

        @Override
        public void apply(@NotNull Style.Builder builder) {
            builder.hoverEvent(HoverEvent.showText(this.value));
        }
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<HoverTag.Action<?>> {

        @Override
        public void decorate(ComponentContext context) {
            final HoverTag.Action<?> data = context.getData();
            data.apply(context.getStyleBuilder());
        }
    }
}
