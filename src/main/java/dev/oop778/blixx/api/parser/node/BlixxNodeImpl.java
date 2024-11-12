package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.parser.TagWithDefinedDataImpl;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.keyedspec.NodeReplacementKeyed;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.adventure.FastComponentBuilder;
import dev.oop778.blixx.util.collection.ObjectArray;
import dev.oop778.blixx.util.adventure.StyleBuilder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@Getter
public abstract class BlixxNodeImpl implements BlixxNode {
    protected final Object key;
    @Setter
    protected BlixxNodeSpec spec;
    @Setter
    protected ObjectArray<BlixxTag.WithDefinedData<?>> tags;
    @Setter
    protected BlixxNodeImpl next;
    @Setter
    protected BlixxNodeImpl previous;
    @Setter
    protected String content = "";
    @Nullable
    protected Component adventureComponent;
    protected boolean hasIndexableTagData = false;

    public BlixxNodeImpl(Object key, BlixxNodeSpec spec) {
        this.key = key;
        this.spec = spec;
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
            final BlixxNodeImpl copy = current.copyMe();

            if (top == null) {
                top = copy;
            }

            if (last != null) {
                last.next = copy;
                copy.previous = last;
            }

            current = current.getNext();
            last = copy;
        }

        return top;
    }

    @Override
    public TextComponent build() {
        final BlixxProcessor.Component.ComponentContext context = BlixxProcessor.Component.ComponentContext.builder()
                .blixx(this.spec.getBlixx())
                .build();

        final FastComponentBuilder rootBuilder = new FastComponentBuilder();
        rootBuilder.setContent("");

        final Iterator<BlixxNodeImpl> iterator = this.iterator(true);

        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            if (node.adventureComponent != null) {
                rootBuilder.append(node.adventureComponent);
                continue;
            }

            context.setNode(node);

            final FastComponentBuilder componentBuilder = new FastComponentBuilder();
            componentBuilder.setContent(node.getContent());

            context.setStyleBuilder(new StyleBuilder());
            context.setComponentBuilder(componentBuilder);

            // Process visitors
            for (final BlixxTag.WithDefinedData<?> tag : node.getTags()) {
                context.setData(tag.getDefinedData());
                context.setTag(tag);

                final BlixxProcessor processor = tag.getProcessor();
                if (processor instanceof BlixxProcessor.Component.Decorator) {
                    ((BlixxProcessor.Component.Decorator) processor).decorate(context);
                }

                if (processor instanceof BlixxProcessor.Component.Visitor) {
                    ((BlixxProcessor.Component.Visitor) processor).visit(context);
                }
            }

            componentBuilder.setStyle(context.getStyleBuilder().build());
            rootBuilder.append(componentBuilder.build());
        }

        return rootBuilder.build();
    }

    @Override
    public boolean hasPlaceholders(boolean withTags) {
        return this.spec.hasPlaceholders(this, withTags);
    }

    public abstract BlixxNodeImpl copyMe();

    public abstract BlixxNodeImpl createNextNode(@Nullable Predicate<BlixxTag.WithDefinedData<?>> tagFilterer);

    public @Nullable Component getAdventureComponent() {
        return this.build();
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

    public boolean hasTag(BlixxTag.WithDefinedData<?> parsedTag) {
        return this.tags != null && this.tags.stream().anyMatch(parsedTag::compare);
    }

    public boolean hasTag(Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
        return this.tags != null && this.tags.stream().anyMatch(tagFilterer);
    }

    public void parseIntoAdventure() {
        if (this.adventureComponent != null) {
            return;
        }

        this.adventureComponent = this.buildAdventure();
    }

    public void replace(List<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        final NodeReplacementKeyed nodeReplacement = new NodeReplacementKeyed(this, placeholders, context);
        nodeReplacement.work();
    }

    public List<BlixxNodeImpl> splitReplace(String what, BlixxNodeImpl with) {
        final List<BlixxNodeImpl> newNodes = new ArrayList<>();
        BlixxNodeImpl currentNode = this;

        while (currentNode != null) {
            final String input = currentNode.content;
            final int startIndex = input.indexOf(what);

            if (startIndex == -1) {
                currentNode = currentNode.next;
                continue;
            }

            // Split the input before and after the found placeholder
            final String before = input.substring(0, startIndex);
            final String after = input.substring(startIndex + what.length());
            final BlixxNodeImpl originalNext = currentNode.next; // copy original next node

            // We just set the node of previous node to copy of with.
            if (after.isEmpty() && before.isEmpty()) {
                final BlixxNodeImpl copy = with.copy();

                // Inherit everything from with copy
                if (currentNode.previous == null) {
                    currentNode.inherit(copy);
                    newNodes.add(currentNode);
                    currentNode.setNodeAsTreeEnd(originalNext);
                    continue;
                }

                newNodes.add(copy);

                currentNode.previous.next = copy;
                copy.previous = currentNode.previous;
                currentNode = copy;

                currentNode.setNodeAsTreeEnd(originalNext);
                continue;
            }

            // After is empty & before is not, we just set content of current node
            if (after.isEmpty()) {
                final BlixxNodeImpl copy = with.copy();
                newNodes.add(copy);

                currentNode.next = copy;
                copy.previous = currentNode;

                currentNode.content = before;
                currentNode = copy;

                currentNode.setNodeAsTreeEnd(originalNext);
                continue;
            }

            // Both before & after are present
            final BlixxNodeImpl afterNode = currentNode.copy();
            afterNode.content = after;

            final BlixxNodeImpl beforeNode = with.copy();

            // Set content of current node to before & update nodes
            currentNode.content = before;
            currentNode.next = beforeNode;
            beforeNode.previous = currentNode;

            final BlixxNodeImpl treeEnd = beforeNode.findTreeEnd();
            treeEnd.next = afterNode;
            afterNode.previous = treeEnd;

            currentNode = beforeNode;
        }

        return newNodes;
    }

    protected Component buildAdventure() {
        final BlixxProcessor.Component.ComponentContext context = BlixxProcessor.Component.ComponentContext.builder().blixx(this.spec.getBlixx()).build();
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
        }

        final Style build = context.getStyleBuilder().build();
        componentBuilder.setStyle(build);

        return componentBuilder.build();
    }

    protected ObjectArray<BlixxTag.WithDefinedData<?>> copyTags() {
        if (!this.hasIndexableTagData) {
            return this.tags == null ? null : new ObjectArray<>(this.tags);
        }

        return this.tags.map(this::copyTag);
    }

    private void inherit(BlixxNodeImpl from) {
        this.content = from.content;
        this.tags = from.tags;
        this.adventureComponent = from.adventureComponent;
        this.hasIndexableTagData = from.hasIndexableTagData;
        this.next = from.next;
    }

    public void setNodeAsTreeEnd(@Nullable BlixxNodeImpl node) {
        if (node == null) {
            return;
        }

        BlixxNodeImpl currentNode = this;
        while (currentNode.next != null) {
            currentNode = currentNode.next;
        }

        node.previous = currentNode;
        currentNode.next = node;
    }

    public BlixxNodeImpl findTreeEnd() {
        BlixxNodeImpl currentNode = this;
        while (currentNode.next != null) {
            currentNode = currentNode.next;
        }

        return currentNode;
    }

    @SuppressWarnings("unchecked")
    private <T> BlixxTag.WithDefinedData<T> copyTag(BlixxTag.WithDefinedData<T> tag) {
        if (!(tag.getDefinedData() instanceof Indexable)) {
            return tag;
        }

        return new TagWithDefinedDataImpl<>(tag, (T) ((Indexable) tag.getDefinedData()).copy());
    }
}
