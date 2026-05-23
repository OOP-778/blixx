package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.component.BlixxClickEvent;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.StringQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class ClickTag implements BlixxTag<ClickTag.Action> {
    public static final ClickTag INSTANCE = new ClickTag();
    public static final Processor PROCESSOR = new Processor();

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    @Override
    public Action createData(@NotNull BlixxProcessor.ParserContext context, @NotNull StringQueue args) {
        final String action = args.pop();
        String value = args.pop();
        if (value.startsWith("\"")) {
            value = value.substring(1);
        }

        if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }

        return new Action(action, value);
    }

    @AllArgsConstructor
    @Getter
    public static class Action implements Indexable.WithStringContent {
        private final String action;
        private String value;

        @Override
        public Indexable copy() {
            return new Action(this.action, this.value);
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
            context.getStyle().clickEvent(BlixxClickEvent.of(data.getAction(), data.getValue()));
        }
    }
}
