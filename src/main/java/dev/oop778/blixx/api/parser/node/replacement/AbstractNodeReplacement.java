package dev.oop778.blixx.api.parser.node.replacement;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.BlixxComponentImpl;
import dev.oop778.blixx.api.component.ComponentDecoration;
import dev.oop778.blixx.api.formatter.BlixxFormatter;
import dev.oop778.blixx.api.formatter.BlixxFormatters;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.BlixxTagHolder;
import dev.oop778.blixx.api.parser.node.kind.BlixxPlaceholderCloseNode;
import dev.oop778.blixx.api.parser.node.kind.BlixxPlaceholderNode;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNodeImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTag;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractNodeReplacement {
    protected final BlixxNodeInternal rootNode;
    protected final Iterable<? extends BlixxPlaceholder<?>> placeholders;
    protected final PlaceholderContext context;
    protected final Map<String, BlixxPlaceholder.Literal<?>> literalPlaceholders;
    protected final List<BlixxPlaceholder.Pattern<?>> patternPlaceholders;

    public AbstractNodeReplacement(
            BlixxNodeInternal rootNode,
            Iterable<? extends BlixxPlaceholder<?>> placeholders,
            PlaceholderContext context) {
        this.rootNode = rootNode;
        this.placeholders = placeholders;
        this.context = context;

        this.literalPlaceholders = new HashMap<>(
                placeholders instanceof Collection
                        ? ((Collection<? extends BlixxPlaceholder<?>>) placeholders).size()
                        : 10);
        this.patternPlaceholders = new ArrayList<>(2);

        for (final BlixxPlaceholder<?> placeholder : placeholders) {
            if (placeholder instanceof BlixxPlaceholder.Literal) {
                for (final String key : ((BlixxPlaceholder.Literal<?>) placeholder).keys()) {
                    this.literalPlaceholders.put(key, (BlixxPlaceholder.Literal<?>) placeholder);
                }

                continue;
            }

            if (placeholder instanceof BlixxPlaceholder.Pattern) {
                this.patternPlaceholders.add((BlixxPlaceholder.Pattern<?>) placeholder);
            }
        }
    }

    public abstract void work();

    protected String statefulReplace(String input, String what, String to) {
        final StringBuilder sb = new StringBuilder(input.length());
        int start = 0;
        int nextMatch;

        while ((nextMatch = input.indexOf(what, start)) != -1) {
            // Check boundary conditions before accessing characters
            if (nextMatch - 1 >= start && nextMatch + what.length() < input.length()) {
                final char characterAtNextMatchMinusOne = input.charAt(nextMatch - 1);
                final char characterAtNextMatchPlusLen = input.charAt(nextMatch + what.length());

                // check if matched `what` is placeholder
                if (!this.isPlaceholderChar(characterAtNextMatchMinusOne)
                        || !this.isPlaceholderChar(characterAtNextMatchPlusLen)) {
                    sb.append(input, start, nextMatch + what.length());
                    start = nextMatch + what.length();
                    continue;
                }
            } else {
                sb.append(input, start, nextMatch + what.length());
                start = nextMatch + what.length();
                continue;
            }

            sb.append(input, start, nextMatch - 1);
            sb.append(to);

            start = nextMatch + what.length() + 1;
        }

        if (input.length() >= start) {
            sb.append(input, start, input.length());
        }

        return sb.toString(); // Missing return statement, and returning a String
    }

    protected void handleObjectReplacement(BlixxPlaceholderNode placeholderNode, String rawPlaceholder, Object value) {
        if (value instanceof BlixxComponentImpl) {
            this.handleComponentReplacement(placeholderNode, (BlixxComponentImpl) value);
            return;
        }

        if (value instanceof ComponentDecoration) {
            this.handleDecorationReplacement(placeholderNode, (ComponentDecoration) value);
            return;
        }

        this.handleStringReplacement(placeholderNode, rawPlaceholder, value);
    }

    protected void handleDecorationReplacement(BlixxPlaceholderNode node, ComponentDecoration decoration) {
        final BlixxNodeInternal next = node.getNext();
        if (next == null) {
            return;
        }

        final Iterable<? extends BlixxTag.WithDefinedData<?>> tags = decoration.getTags(node.blixx());
        if (node.getClosingKey() == null) {
            this.addOrReplaceTags(next, tags);

        } else {
            BlixxNodeInternal current = next;
            while (current != null) {
                this.addOrReplaceTags(current, tags);

                current = current.getNext();
                if (current instanceof BlixxPlaceholderCloseNode
                        && ((BlixxPlaceholderCloseNode) current)
                                .getIdentityObject()
                                .equals(node.getClosingKey())) {
                    break;
                }
            }
        }

        this.pop(node);
    }

    protected void addOrReplaceTags(BlixxNodeInternal node, Iterable<? extends BlixxTag.WithDefinedData<?>> tags) {
        if (!(node instanceof BlixxTagHolder)) {
            return;
        }

        for (final BlixxTag.WithDefinedData<?> tag : tags) {
            ((BlixxTagHolder) node).replaceOrAddTag(tag);
        }
    }

    protected void handleStringReplacement(BlixxPlaceholderNode node, String placeholder, Object value) {
        final BlixxTextNodeImpl replacementNode = new BlixxTextNodeImpl(String.valueOf(value), node.getTags());
        replacementNode.setBlixx(node.blixx());

        replacementNode.setPrevious(node.getPrevious());
        replacementNode.setNext(node.getNext());

        if (node.getPrevious() != null) {
            node.getPrevious().setNext(replacementNode);
        }

        if (node.getNext() != null) {
            node.getNext().setPrevious(replacementNode);
        }

        if (node.getPrevious() == null) {
            node.setRootNodeReplacement(replacementNode);
        }
    }

    protected void postNewNode(BlixxNodeInternal node) {}

    protected void handleObjectReplacementInString(
            Indexable.WithStringContent<?> stringContent, String placeholder, Object value) {
        if (value instanceof BlixxComponent || value instanceof BlixxNode) {
            throw new IllegalStateException("Can only replace plain objects in strings");
        }

        final String replaced = this.statefulReplace(stringContent.getContent(), placeholder, String.valueOf(value));
        if (replaced == null) {
            return;
        }

        stringContent.setContent(replaced);
    }

    protected void handleLiteralReplacement(
            String rawPlaceholder,
            Collection<Indexable<?>> nodes,
            BlixxPlaceholder.Literal<?> placeholder,
            PlaceholderContext context) {
        final Object value = formatPlaceholder(placeholder.get(context));
        for (final Indexable<?> node : nodes) {
            this.handleReplacement(rawPlaceholder, node, value);
        }
    }

    private Object formatPlaceholder(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String || value instanceof BlixxNode) {
            return value;
        }

        final BlixxFormatters formatters =
                this.rootNode.blixx().placeholderConfig().defaultFormatters();
        if (formatters == null) {
            return value;
        }

        final BlixxFormatter blixxFormatter = formatters.find(value.getClass());
        if (blixxFormatter == null) {
            return value;
        }

        return blixxFormatter.apply(value);
    }

    protected void handleReplacement(String rawPlaceholder, Indexable<?> in, Object value) {
        if (in instanceof BlixxPlaceholderNode) {
            this.handleObjectReplacement((BlixxPlaceholderNode) in, rawPlaceholder, value);
            return;
        }

        if (in instanceof Indexable.WithStringContent) {
            this.handleObjectReplacementInString((Indexable.WithStringContent<?>) in, rawPlaceholder, value);
            return;
        }

        throw new IllegalStateException("Can only replace objects in nodes or strings");
    }

    protected void handlePatternReplacement(
            String rawPlaceholder,
            Collection<Indexable<?>> indexables,
            BlixxPlaceholder.Pattern<?> placeholder,
            PlaceholderContext context) {
        final Pattern pattern = placeholder.pattern();
        final Matcher matcher = pattern.matcher(rawPlaceholder);

        final PlaceholderContext compose = PlaceholderContext.compose(PlaceholderContext.create(matcher), context);
        while (matcher.find()) {
            final Object value = formatPlaceholder(placeholder.get(compose));
            for (final Indexable<?> node : indexables) {
                this.handleReplacement(matcher.group(), node, value);
            }
        }
    }

    private void pop(BlixxPlaceholderNode node) {
        final BlixxNodeInternal next = node.getNext();
        if (next != null) {
            next.setPrevious(node.getPrevious());
        }

        final BlixxNodeInternal previous = node.getPrevious();
        if (previous != null) {
            previous.setNext(node.getNext());
        }

        if (previous == null) {
            node.setRootNodeReplacement(next);
        }
    }

    private boolean isPlaceholderChar(char character) {
        return this.rootNode.blixx().parserConfig().placeholderCharacters().contains(character);
    }

    private void handleComponentReplacement(BlixxPlaceholderNode node, BlixxComponentImpl with) {
        final BlixxNodeInternal replacementNode =
                (BlixxNodeInternal) with.copy().getNode();
        this.postNewNode(replacementNode);

        final BlixxNodeInternal treeEnd = replacementNode.findTreeEnd();
        if (replacementNode instanceof BlixxTagHolder && !((BlixxTagHolder) replacementNode).hasTags()) {
            ((BlixxTagHolder) replacementNode).setTags(node.getTags());
        }

        replacementNode.setPrevious(node.getPrevious());
        treeEnd.setNext(node.getNext());

        if (node.getPrevious() != null) {
            node.getPrevious().setNext(replacementNode);
        }

        if (node.getNext() != null) {
            treeEnd.setNext(node.getNext());
            node.getNext().setPrevious(treeEnd);
        }

        if (node.getPrevious() == null) {
            node.setRootNodeReplacement(replacementNode);
        }
    }
}
