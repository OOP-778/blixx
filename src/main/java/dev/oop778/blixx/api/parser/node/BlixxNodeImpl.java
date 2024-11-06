package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.TagWithDefinedDataImpl;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.indexable.IndexableKey;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.FastComponentBuilder;
import dev.oop778.blixx.util.ObjectArray;
import dev.oop778.blixx.util.StyleBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class BlixxNodeImpl implements BlixxNode {
    private final IndexableKey key;
    private final BlixxNodeSpec spec;
    @Setter
    protected ObjectArray<BlixxTag.WithDefinedData<?>> tags;
    @Setter
    protected BlixxNodeImpl next;
    @Setter
    protected String content = "";
    @Nullable
    protected Component adventureComponent;
    private boolean hasIndexableTagData = false;

    public BlixxNodeImpl clone() {
        final BlixxNodeImpl copy = new BlixxNodeImpl(this.key, this.spec);
        copy.content = this.content;
        copy.tags = this.copyTags();
        copy.adventureComponent = this.adventureComponent;

        return copy;
    }

    @Override
    public ObjectArray<BlixxTag.WithDefinedData<?>> getTags() {
        return this.tags != null ? this.tags : (ObjectArray<BlixxTag.WithDefinedData<?>>) ObjectArray.EMPTY;
    }

    @Override
    public BlixxNodeImpl copy() {
        BlixxNodeImpl top = null;
        BlixxNodeImpl current = this;
        BlixxNodeImpl last = null;

        while (current != null) {
            final BlixxNodeImpl copy = current.clone();

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

    @Override
    public TextComponent build() {
        Component current = null;
        final BlixxProcessor.Component.ComponentContext context = BlixxProcessor.Component.ComponentContext.builder().build();

        final Iterator<BlixxNodeImpl> iterator = this.iterator(true);
        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            if (node.adventureComponent != null) {
                current = current == null ? node.adventureComponent : current.append(node.adventureComponent);
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
            current = current == null ? componentBuilder.build() : current.append(componentBuilder.build());
        }

        return (TextComponent) current;
    }

    @Override
    public boolean hasPlaceholders() {
        return this.spec.hasPlaceholders(this);
    }

    public void addTag(BlixxTag.WithDefinedData<?> tag) {
        if (this.tags == null) {
            this.tags = new ObjectArray<>(2);
        }

        if (tag.getDefinedData() instanceof Indexable) {
            this.hasIndexableTagData = true;
        }

        this.tags.add(tag);
    }

    public Iterator<BlixxNodeImpl> iterator(boolean withItself) {
        return new Iterator<>() {
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

    public BlixxNodeImpl createNextNode(IndexableKey key, @Nullable Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
        final BlixxNodeImpl next = new BlixxNodeImpl(key, this.spec);
        if (this.tags != null) {
            next.tags = tagFilterer == null ? new ObjectArray<>(this.tags) : this.tags.filter(tagFilterer);
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

    public void replace(List<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        final NodeReplacementWorkV2 nodeReplacement = new NodeReplacementWorkV2(this, placeholders, context);
        nodeReplacement.work();
    }

    public boolean splitReplace(String what, BlixxNodeImpl with) {
        BlixxNodeImpl currentNode = this;
        boolean replaced = false;

        while (currentNode != null) {
            final String input = currentNode.content;
            final int startIndex = input.indexOf(what);

            if (startIndex == -1) {
                currentNode = currentNode.next;
                continue;
            }

            replaced = true;

            // Split the input before and after the found placeholder
            final String before = input.substring(0, startIndex); // text before the placeholder
            final String after = input.substring(startIndex + what.length()); // text after the placeholder

            // Set the content of the current node to 'before'
            currentNode.content = before;

            // Preserve the original next node (if it exists)
            final BlixxNodeImpl originalNext = currentNode.next;

            // Link the replacement node chain to the current node
            currentNode.next = with.copy();

            // Move to the end of the replacement chain
            BlixxNodeImpl lastNode = currentNode;
            while (lastNode.next != null) {
                lastNode = lastNode.next;
            }

            if (after.isEmpty()) {
                lastNode.next = originalNext;
                break;
            }

            // Create a node for the remaining part of the string and link it
            final BlixxNodeImpl afterNode = this.copy(); // Link to the preserved original next node
            afterNode.content = after;
            afterNode.next = originalNext;
            lastNode.next = afterNode;

            currentNode = currentNode.next;
        }

        return replaced;
    }

    public Map<String, List<Indexable>> collectPlaceholders() {
        final Iterator<BlixxNodeImpl> iterator = this.iterator(true);
        final Map<String, List<Indexable>> placeholderToNode = new HashMap<>();

        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            node.getSpec().collectPlaceholders(node, placeholderToNode);
        }

        return placeholderToNode;
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

    private ObjectArray<BlixxTag.WithDefinedData<?>> copyTags() {
        if (!this.hasIndexableTagData) {
            return this.tags == null ? null : new ObjectArray<>(this.tags);
        }

        return this.tags.map(this::copyTag);
    }

    @SuppressWarnings("unchecked")
    private <T> BlixxTag.WithDefinedData<T> copyTag(BlixxTag.WithDefinedData<T> tag) {
        if (!(tag.getDefinedData() instanceof Indexable)) {
            return tag;
        }

        return new TagWithDefinedDataImpl<>(tag, (T) ((Indexable) tag.getDefinedData()).copy());
    }
}
