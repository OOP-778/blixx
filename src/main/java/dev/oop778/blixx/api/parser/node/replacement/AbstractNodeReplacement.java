package dev.oop778.blixx.api.parser.node.replacement;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.BlixxComponentImpl;
import dev.oop778.blixx.api.component.ComponentDecoration;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.ObjectArray;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public abstract class AbstractNodeReplacement {
    protected final BlixxNodeImpl rootNode;
    protected final List<? extends BlixxPlaceholder<?>> placeholders;
    protected final PlaceholderContext context;

    public abstract void work();

    protected void handleObjectReplacement(Indexable.WithNodeContent withNodeContent, String placeholder, Object value) {
        final BlixxNodeImpl node = (BlixxNodeImpl) withNodeContent.getNode();
        if (value instanceof BlixxComponentImpl) {
            this.handleComponentReplacement(placeholder, (BlixxComponentImpl) value, node);
            return;
        }

        if (value instanceof ComponentDecoration) {
            this.handleDecorationReplacement(node, placeholder, (ComponentDecoration) value);
            return;
        }

        this.handleStringReplacement(node, placeholder, value);
    }

    protected void handleDecorationReplacement(BlixxNodeImpl node, String placeholder, ComponentDecoration decoration) {
        final int startIndex = node.getContent().indexOf(placeholder);
        if (startIndex == -1) {
            return;
        }

        node.setContent(node.getContent().replace(placeholder,""));

        final ObjectArray<BlixxTag.WithDefinedData<?>> matchTags = node.getTags().copy();
        BlixxNodeImpl current = node;

        while (current != null) {
            if (!current.getTags().equals(matchTags)) {
                return;
            }

            final Iterable<? extends BlixxTag.WithDefinedData<?>> tags = decoration.getTags(this.rootNode.getSpec().getBlixx());
            for (final BlixxTag.WithDefinedData<?> tag : tags) {
                current.addTag(tag);
            }

            current = current.getNext();
        }
    }

    protected void handleStringReplacement(BlixxNodeImpl node, String placeholder, Object value) {
        final String replace = node.getContent().replace(placeholder, String.valueOf(value));
        if (replace.equals(node.getContent())) {
            return;
        }

        node.setContent(replace);
        this.postSuccessfulReplacement(node);
    }

    protected void postSuccessfulReplacement(Indexable indexable) {}

    protected void handleObjectReplacementInString(Indexable.WithStringContent stringContent, String placeholder, Object value) {
        if (value instanceof BlixxComponent || value instanceof BlixxNode) {
            throw new IllegalStateException("Can only replace plain objects in strings");
        }

        final String content = stringContent.getContent();
        final String replaced = content.replace(placeholder, String.valueOf(value));
        if (replaced.equals(content)) {
            return;
        }

        stringContent.setContent(replaced);
        this.postSuccessfulReplacement(stringContent);
    }

    protected void handleLiteralReplacement(String fullStringPlaceholder, Iterable<Indexable> nodes, BlixxPlaceholder.Literal<?> placeholder, PlaceholderContext context) {
        final Collection<String> keys = placeholder.keys();
        if (!keys.contains(fullStringPlaceholder.substring(1, fullStringPlaceholder.length() - 1))) {
            return;
        }

        final Object value = placeholder.get(context);
        for (final Indexable indexable : nodes) {
            this.handleReplacement(fullStringPlaceholder, indexable, value);
        }
    }

    protected void handleReplacement(String fullPlaceholder, Indexable indexable, Object value) {
        if (indexable instanceof Indexable.WithNodeContent) {
            this.handleObjectReplacement((Indexable.WithNodeContent) indexable, fullPlaceholder, value);
            return;
        }

        if (indexable instanceof Indexable.WithStringContent) {
            this.handleObjectReplacementInString((Indexable.WithStringContent) indexable, fullPlaceholder, value);
            return;
        }

        throw new IllegalStateException("Can only replace objects in nodes or strings");
    }

    protected void handlePatternReplacement(String fullStringPlaceholder, Iterable<Indexable> nodes, BlixxPlaceholder.Pattern<?> placeholder, PlaceholderContext context) {
        final Pattern pattern = placeholder.pattern();
        final Matcher matcher = pattern.matcher(fullStringPlaceholder.substring(1, fullStringPlaceholder.length() - 1));

        final PlaceholderContext compose = PlaceholderContext.compose(PlaceholderContext.create(matcher), context);
        while (matcher.find()) {
            final Object value = placeholder.get(compose);
            for (final Indexable indexable : nodes) {
                this.handleReplacement(fullStringPlaceholder, indexable, value);
            }
        }
    }

    private void handleComponentReplacement(String placeholder, BlixxComponentImpl value, BlixxNodeImpl node) {
        for (final BlixxNodeImpl blixxNode : node.splitReplace(placeholder, value.getNode())) {
            this.postSuccessfulReplacement(blixxNode);
        }

    }
}
