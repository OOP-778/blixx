package dev.oop778.blixx.api.parser;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponentImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.api.util.FastComponentBuilder;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class BlixxNodeImpl {
    private final NodeKey key;
    private final BlixxNodeSpec spec;
    protected List<BlixxTag.WithDefinedData<?>> tags;
    @Setter
    protected BlixxNodeImpl next;
    @Setter
    protected String content = "";
    @Nullable
    protected Component adventureComponent;

    public BlixxNodeImpl clone() {
        final BlixxNodeImpl copy = new BlixxNodeImpl(this.key, this.spec);
        copy.content = this.content;
        copy.tags = this.tags != null ? new ArrayList<>(this.tags) : null;
        copy.adventureComponent = this.adventureComponent;

        return copy;
    }

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

    public BlixxNodeImpl copy() {
        return this.clone();
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

    public BlixxNodeImpl createNextNode(NodeKey key, @Nullable Predicate<BlixxTag<?>> tagFilterer) {
        final BlixxNodeImpl next = new BlixxNodeImpl(key, this.spec);
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

    public boolean hasPlaceholders() {
        return this.spec.hasPlaceholders(this);
    }

    public void replace(List<BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        final Iterator<BlixxNodeImpl> iterator = this.iterator(true);
        final Map<String, List<BlixxNodeImpl>> placeholderToNode = new HashMap<>();

        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            final List<String> nodePlaceholders = node.getPlaceholders();
            if (nodePlaceholders == null) {
                continue;
            }

            for (final String nodePlaceholder : nodePlaceholders) {
                placeholderToNode.computeIfAbsent(nodePlaceholder, ($) -> new ArrayList<>()).add(node);
            }
        }

        for (final Map.Entry<String, List<BlixxNodeImpl>> entry : placeholderToNode.entrySet()) {
            final String fullPlaceholder = entry.getKey();
            final String placeholder = fullPlaceholder.substring(1, fullPlaceholder.length() - 1);

            for (final BlixxPlaceholder<?> blixxPlaceholder : placeholders) {
                if (blixxPlaceholder instanceof BlixxPlaceholder.Literal<?>) {
                    final Collection<String> keys = ((BlixxPlaceholder.Literal<?>) blixxPlaceholder).keys();
                    if (!keys.contains(placeholder)) {
                        continue;
                    }

                    final Object value = blixxPlaceholder.get(context);
                    for (final BlixxNodeImpl node : entry.getValue()) {
                        if (value instanceof BlixxComponentImpl) {
                            node.splitReplace(fullPlaceholder, ((BlixxComponentImpl) value).getNode());
                        } else {
                            node.setContent(node.getContent().replace(fullPlaceholder, String.valueOf(value)));
                        }
                    }
                }

                if (blixxPlaceholder instanceof BlixxPlaceholder.Pattern<?>) {
                    final Pattern pattern = ((BlixxPlaceholder.Pattern<?>) blixxPlaceholder).pattern();
                    final Matcher matcher = pattern.matcher(placeholder);

                    final PlaceholderContext compose = PlaceholderContext.compose(PlaceholderContext.create(matcher), context);
                    while (matcher.find()) {
                        final Object value = blixxPlaceholder.get(compose);
                        for (final BlixxNodeImpl node : entry.getValue()) {
                            if (value instanceof BlixxComponentImpl) {
                                node.splitReplace(matcher.group(), ((BlixxComponentImpl) value).getNode());
                            } else {
                                node.setContent(node.getContent().replace(matcher.group(), String.valueOf(value)));
                            }
                        }
                    }
                }
            }
        }
    }

    public void splitReplace(String what, BlixxNodeImpl with) {
        BlixxNodeImpl currentNode = this;

        while (currentNode != null) {
            final String input = currentNode.content;
            final int startIndex = input.indexOf(what);

            if (startIndex == -1) {
                currentNode = currentNode.next;
                continue;
            }

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

    private List<String> getPlaceholders() {
        return this.spec.getPlaceholders(this);
    }
}
