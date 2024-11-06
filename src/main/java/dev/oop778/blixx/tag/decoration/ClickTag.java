package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.indexable.IndexableKey;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

public class ClickTag implements BlixxTag<ClickTag.Action> {
    public static final ClickTag INSTANCE = new ClickTag();
    public static final Processor PROCESSOR = new Processor();

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    @Override
    public Action createData(@NotNull BlixxProcessor.ParserContext context, @NotNull BaseArgumentQueue args) {
        final String action = args.pop();
        final String value = args.pop();
        return new Action(ClickEvent.Action.valueOf(action.toUpperCase()), context.createKey(), value);
    }

    @AllArgsConstructor
    @Getter
    public static class Action implements Indexable.WithStringContent {
        private final ClickEvent.Action action;
        private final IndexableKey key;
        private String value;

        @Override
        public Indexable copy() {
            return new Action(this.action, this.key, this.value);
        }

        @Override
        public String getContent() {
            return this.value;
        }

        @Override
        public void setContent(String content) {
            this.value = content;
        }
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<Action> {
        @Override
        public void decorate(@NonNull ComponentContext context) {
            final Action data = context.getData();
            context.getStyleBuilder().clickEvent(ClickEvent.clickEvent(data.getAction(), data.getValue()));
        }
    }
}
