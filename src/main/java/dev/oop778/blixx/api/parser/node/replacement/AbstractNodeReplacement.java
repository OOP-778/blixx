package dev.oop778.blixx.api.parser.node.replacement;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.BlixxComponentImpl;
import dev.oop778.blixx.api.component.ComponentDecoration;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.parser.node.IndexedPlaceholder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.collection.ObjectArray;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractNodeReplacement {
    protected final BlixxNodeImpl rootNode;
    protected final Iterable<? extends BlixxPlaceholder<?>> placeholders;
    protected final PlaceholderContext context;
    protected final Map<String, BlixxPlaceholder.Literal<?>> literalPlaceholders;
    protected final List<BlixxPlaceholder.Pattern<?>> patternPlaceholders;

    public AbstractNodeReplacement(BlixxNodeImpl rootNode, Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        this.rootNode = rootNode;
        this.placeholders = placeholders;
        this.context = context;

        this.literalPlaceholders = new HashMap<>(placeholders instanceof Collection ? ((Collection<? extends BlixxPlaceholder<?>>) placeholders).size() : 10);
        this.patternPlaceholders = new ArrayList<>();

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
        final StringBuilder sb = new StringBuilder(input.length() + what.length());
        int start = 0;
        int nextMatch;
        boolean replaced = false;

        while ((nextMatch = input.indexOf(what, start)) != -1) {
            sb.append(input, start, nextMatch);
            sb.append(to);

            start = nextMatch + what.length();
            replaced = true;
        }

        if (input.length() >= start) {
            sb.append(input, start, input.length());
        }

        return !replaced ? null : sb.toString();
    }

    protected void handleObjectReplacement(Indexable.WithNodeContent withNodeContent, String match, Object value) {
        final BlixxNodeImpl node = (BlixxNodeImpl) withNodeContent.getNode();
        if (value instanceof BlixxComponentImpl) {
            this.handleComponentReplacement(match, (BlixxComponentImpl) value, node);
            return;
        }

        if (value instanceof ComponentDecoration) {
            this.handleDecorationReplacement(node, match, (ComponentDecoration) value);
            return;
        }

        this.handleStringReplacement(node, match, value);
    }

    protected void handleDecorationReplacement(BlixxNodeImpl node, String placeholder, ComponentDecoration decoration) {
        for (final BlixxNodeImpl blixxNode : node.splitRemoveApplyDecoration(placeholder, decoration)) {
            this.postSuccessfulReplacement(blixxNode);
        }
    }

    protected void handleStringReplacement(BlixxNodeImpl node, String placeholder, Object value) {
        final String replaced = this.statefulReplace(node.getContent(), placeholder, String.valueOf(value));
        if (replaced == null) {
            return;
        }

        node.setContent(replaced);
        this.postSuccessfulReplacement(node);
    }

    protected void postSuccessfulReplacement(Indexable indexable) {}

    protected void handleObjectReplacementInString(Indexable.WithStringContent stringContent, String placeholder, Object value) {
        if (value instanceof BlixxComponent || value instanceof BlixxNode) {
            throw new IllegalStateException("Can only replace plain objects in strings");
        }

        final String replaced = this.statefulReplace(stringContent.getContent(), placeholder, String.valueOf(value));
        if (replaced == null) {
            return;
        }

        stringContent.setContent(replaced);
        this.postSuccessfulReplacement(stringContent);
    }

    protected void handleLiteralReplacement(String placeholderKey, IndexedPlaceholder indexedPlaceholder, BlixxPlaceholder.Literal<?> placeholder, PlaceholderContext context) {
        final Object value = placeholder.get(context);
        for (final IndexedPlaceholder.Entry entry : indexedPlaceholder.getEntries()) {
            this.handleReplacement(entry.compileReplacement(placeholderKey), entry.getIndexable(), value);
        }
    }

    protected void handleReplacement(String match, Indexable in, Object value) {
        if (in instanceof Indexable.WithNodeContent) {
            this.handleObjectReplacement((Indexable.WithNodeContent) in, match, value);
            return;
        }

        if (in instanceof Indexable.WithStringContent) {
            this.handleObjectReplacementInString((Indexable.WithStringContent) in, match, value);
            return;
        }

        throw new IllegalStateException("Can only replace objects in nodes or strings");
    }

    protected void handlePatternReplacement(String rawPlaceholder, IndexedPlaceholder indexedPlaceholder, BlixxPlaceholder.Pattern<?> placeholder, PlaceholderContext context) {
        final Pattern pattern = placeholder.pattern();
        final Matcher matcher = pattern.matcher(rawPlaceholder);

        final PlaceholderContext compose = PlaceholderContext.compose(PlaceholderContext.create(matcher), context);
        while (matcher.find()) {
            final Object value = placeholder.get(compose);
            for (final IndexedPlaceholder.Entry entry : indexedPlaceholder.getEntries()) {
                this.handleReplacement(entry.compileReplacement(rawPlaceholder), entry.getIndexable(), value);
            }
        }
    }

    private void handleComponentReplacement(String placeholder, BlixxComponentImpl value, BlixxNodeImpl node) {
        for (final BlixxNodeImpl blixxNode : node.splitReplaceRestricted(placeholder, value.getNode())) {
            this.postSuccessfulReplacement(blixxNode);
        }
    }
}
