package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.BlixxComponentImpl;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeReplacementWorkV2 {
    private final BlixxNodeImpl rootNode;
    private final List<? extends BlixxPlaceholder<?>> placeholders;
    private final PlaceholderContext context;
    private final Map<String, List<Indexable>> placeholderToNode;
    private final Set<Indexable> toCheckForNewPlaceholders;

    public NodeReplacementWorkV2(BlixxNodeImpl node, List<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        this.rootNode = node;
        this.placeholders = placeholders;
        this.context = context;
        this.placeholderToNode = node.collectPlaceholders();
        this.toCheckForNewPlaceholders = new HashSet<>();
    }

    public void work() {
        while (!this.placeholderToNode.isEmpty()) {
            for (final Map.Entry<String, List<Indexable>> entry : this.placeholderToNode.entrySet()) {
                final String fullPlaceholder = entry.getKey();

                for (final BlixxPlaceholder<?> blixxPlaceholder : this.placeholders) {
                    if (blixxPlaceholder instanceof BlixxPlaceholder.Literal<?>) {
                        this.handleLiteralReplacement(fullPlaceholder, entry.getValue(), (BlixxPlaceholder.Literal<?>) blixxPlaceholder, this.context);
                        continue;
                    }

                    if (blixxPlaceholder instanceof BlixxPlaceholder.Pattern<?>) {
                        this.handlePatternReplacement(fullPlaceholder, entry.getValue(), (BlixxPlaceholder.Pattern<?>) blixxPlaceholder, this.context);
                    }
                }
            }

            this.placeholderToNode.clear();
            if (!this.toCheckForNewPlaceholders.isEmpty()) {
                this.checkForNewPlaceholders();
            }
        }
    }

    private void checkForNewPlaceholders() {
        for (final Indexable indexable : this.toCheckForNewPlaceholders) {
            BlixxNodeSpec.findNewPlaceholders(indexable, this.placeholderToNode, this.rootNode.getSpec().getBlixx().parserConfig().placeholderFormats());
        }
    }

    private void handleObjectReplacement(Indexable.WithNodeContent withNodeContent, String placeholder, Object value) {
        final BlixxNodeImpl node = (BlixxNodeImpl) withNodeContent.getNode();
        if (value instanceof BlixxComponentImpl) {
            if (node.splitReplace(placeholder, ((BlixxComponentImpl) value).getNode())) {
                if (BlixxNodeSpec.findAnyPlaceholders(withNodeContent, node.getSpec().getBlixx().parserConfig().placeholderFormats())) {
                    this.toCheckForNewPlaceholders.add(node);
                }
            }
        } else {
            final String replace = node.getContent().replace(placeholder, String.valueOf(value));
            if (replace.equals(node.getContent())) {
                return;
            }

            node.setContent(replace);
            if (BlixxNodeSpec.findAnyPlaceholders(withNodeContent, node.getSpec().getBlixx().parserConfig().placeholderFormats())) {
                this.toCheckForNewPlaceholders.add(node);
            }
        }
    }

    private void handleObjectReplacementInString(Indexable.WithStringContent stringContent, String placeholder, Object value) {
        if (value instanceof BlixxComponent || value instanceof BlixxNode) {
            throw new IllegalStateException("Can only replace plain objects in strings");
        }

        stringContent.setContent(stringContent.getContent().replace(placeholder, String.valueOf(value)));
    }

    private void handleLiteralReplacement(String fullStringPlaceholder, List<Indexable> nodes, BlixxPlaceholder.Literal<?> placeholder, PlaceholderContext context) {
        final Collection<String> keys = placeholder.keys();
        if (!keys.contains(fullStringPlaceholder.substring(1, fullStringPlaceholder.length() - 1))) {
            return;
        }

        final Object value = placeholder.get(context);
        for (final Indexable indexable : nodes) {
            this.handleReplacement(fullStringPlaceholder, indexable, value);
        }
    }

    private void handleReplacement(String fullPlaceholder, Indexable indexable, Object value) {
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

    private void handlePatternReplacement(String fullStringPlaceholder, List<Indexable> nodes, BlixxPlaceholder.Pattern<?> placeholder, PlaceholderContext context) {
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
}
