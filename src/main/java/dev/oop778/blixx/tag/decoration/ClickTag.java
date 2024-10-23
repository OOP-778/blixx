package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class ClickTag implements BlixxTag<ClickTag.Action> {
    public static final ClickTag INSTANCE = new ClickTag();
    public static final Processor PROCESSOR = new Processor();

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    @Override
    public Action createData(@NotNull BlixxProcessor.Context context, @NotNull BaseArgumentQueue args) {
        final String action = args.pop();
        final String value = args.pop();
        return new Action(ClickEvent.Action.valueOf(action.toUpperCase()), value);
    }

    @RequiredArgsConstructor
    @Getter
    public static class Action {
        private final ClickEvent.Action action;
        private final String value;
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<Action> {

        @Override
        public void decorate(@NonNull ComponentContext context) {
            final Action data = context.getData();
            context.getStyleBuilder().clickEvent(ClickEvent.clickEvent(data.getAction(), data.getValue()));
        }
    }
}
