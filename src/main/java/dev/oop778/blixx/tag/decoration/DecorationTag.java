package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import lombok.NonNull;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class DecorationTag implements BlixxTag<TextDecoration> {
    public static final DecorationTag INSTANCE = new DecorationTag();
    public static final Processor PROCESSOR = new Processor();

    @Override
    public TextDecoration createData(BlixxProcessor.@NonNull Context context, @NotNull BaseArgumentQueue args) {
        final String decoration = args.pop();
        return TextDecoration.valueOf(decoration.toUpperCase());
    }

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<TextDecoration> {

        @Override
        public void decorate(@NonNull ComponentContext context) {
            context.getStyleBuilder().decorate((TextDecoration) context.getData());
        }
    }
}
