package dev.oop778.blixx.api.parser;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.api.util.FastComponentBuilder;
import dev.oop778.blixx.util.StyleBuilder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class BlixxNodeImpl {
    private List<BlixxTag.WithDefinedData<?>> tags;
    @Setter
    private BlixxNodeImpl next;
    @Setter
    private String content = "";
    @Nullable
    private Component adventureComponent;

    public void addTag(BlixxTag.WithDefinedData<?> tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }

        this.tags.add(tag);
    }

    public List<BlixxTag.WithDefinedData<?>> getTags() {
        return this.tags != null ? this.tags : Collections.emptyList();
    }

    public Iterator<BlixxNodeImpl> iterator(boolean withItself) {
        return new Iterator<BlixxNodeImpl>() {
            private BlixxNodeImpl current = withItself ? BlixxNodeImpl.this : BlixxNodeImpl.this.next;

            @Override
            public boolean hasNext() {
                return this.current != null;
            }

            @Override
            public BlixxNodeImpl next() {
                if (this.current == null) {
                    throw new NoSuchElementException();
                }

                final BlixxNodeImpl nextNode = this.current;
                this.current = this.current.getNext();
                return nextNode;
            }
        };
    }

    public BlixxNodeImpl copy() {
        BlixxNodeImpl top = null;
        BlixxNodeImpl current = this;
        BlixxNodeImpl last = null;

        while (current != null) {
            final BlixxNodeImpl copy = new BlixxNodeImpl();
            copy.content = current.content;
            copy.tags = current.tags != null ? new ArrayList<>(current.tags) : null;
            copy.adventureComponent = current.adventureComponent;

            if (top == null) {
                top = copy;
            }

            if (last != null) {
                last.next = copy;
            }

            current = current.getNext();
            last = copy;
        }

        return top;
    }

    public TextComponent build() {
        final TextComponent.Builder rootBuilder = Component.text();
        final BlixxProcessor.Component.ComponentContext context = BlixxProcessor.Component.ComponentContext.builder().build();

        final Iterator<BlixxNodeImpl> iterator = this.iterator(true);
        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            if (node.adventureComponent != null) {
                rootBuilder.append(node.adventureComponent);
                continue;
            }

            final FastComponentBuilder componentBuilder = new FastComponentBuilder();
            componentBuilder.setContent(node.getContent());

            context.setStyleBuilder(new StyleBuilder());
            context.setComponentBuilder(componentBuilder);

            for (final BlixxTag.WithDefinedData<?> tag : node.getTags()) {
                context.setData(tag.getDefinedData());

                final BlixxProcessor processor = tag.getProcessor();
                if (processor instanceof BlixxProcessor.Component.Decorator) {
                    ((BlixxProcessor.Component.Decorator) processor).decorate(context);
                }

                if (processor instanceof BlixxProcessor.Component.Visitor<?>) {
                    ((BlixxProcessor.Component.Visitor) processor).visit(context);
                }
            }

            componentBuilder.setStyle(context.getStyleBuilder().build());
            rootBuilder.append(componentBuilder.build());
        }

        return rootBuilder.build();
    }

    public BlixxNodeImpl createNextNode(@Nullable Predicate<BlixxTag<?>> tagFilterer) {
        final BlixxNodeImpl next = new BlixxNodeImpl();
        if (this.tags != null) {
            next.tags = tagFilterer == null ? new ArrayList<>(this.tags) : this.tags.stream().filter(tagFilterer).collect(Collectors.toList());
        }

        this.next = next;
        return next;
    }

    public boolean hasTag(BlixxTag.WithDefinedData<?> parsedTag) {
        return this.tags != null && this.tags.stream().anyMatch(tag -> tag.compareWithData(parsedTag));
    }

    public void parseIntoAdventure(Blixx blixx) {
        if (this.adventureComponent != null) {
            return;
        }


        this.adventureComponent = this.buildAdventure(blixx);
    }

    protected Component buildAdventure(Blixx blixx) {
        final BlixxProcessor.Component.ComponentContext context = BlixxProcessor.Component.ComponentContext.builder().blixx(blixx).build();
        final FastComponentBuilder componentBuilder = new FastComponentBuilder();
        componentBuilder.setContent(this.content);

        context.setComponentBuilder(componentBuilder);
        context.setStyleBuilder(new StyleBuilder());

        for (final BlixxTag.WithDefinedData<?> tag : this.getTags()) {
            final BlixxProcessor processor = tag.getProcessor();
            context.setData(tag.getDefinedData());

            if (processor instanceof BlixxProcessor.Component.Decorator) {
                ((BlixxProcessor.Component.Decorator) processor).decorate(context);
            }

            if (processor instanceof BlixxProcessor.Component.Visitor<?>) {
                ((BlixxProcessor.Component.Visitor) processor).visit(context);
            }
        }

        final Style build = context.getStyleBuilder().build();
        componentBuilder.setStyle(build);

        return componentBuilder.build();
    }
}
