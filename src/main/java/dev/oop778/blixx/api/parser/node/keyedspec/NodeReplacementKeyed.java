package dev.oop778.blixx.api.parser.node.keyedspec;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.parser.node.IndexedPlaceholder;
import dev.oop778.blixx.api.parser.node.replacement.AbstractNodeReplacement;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;

import java.util.*;

public class NodeReplacementKeyed extends AbstractNodeReplacement {
    private final Map<String, IndexedPlaceholder> placeholderToNode;
    private final Set<Indexable> toCheckForNewPlaceholders;

    public NodeReplacementKeyed(BlixxNodeImpl node, Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        super(node, placeholders, context);
        this.placeholderToNode = this.collectInitialPlaceholders();
        this.toCheckForNewPlaceholders = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    @Override
    public void work() {
        while (!this.placeholderToNode.isEmpty()) {
            for (final Map.Entry<String, IndexedPlaceholder> entry : this.placeholderToNode.entrySet()) {
                final String placeholder = entry.getKey();

                final BlixxPlaceholder.Literal<?> literal = this.literalPlaceholders.get(placeholder);
                if (literal != null) {
                    this.handleLiteralReplacement(placeholder, entry.getValue(), literal, this.context);
                    continue;
                }

                for (final BlixxPlaceholder.Pattern<?> patternPlaceholder : this.patternPlaceholders) {
                    this.handlePatternReplacement(placeholder, entry.getValue(), patternPlaceholder, this.context);
                }
            }

            this.placeholderToNode.clear();

            if (!this.toCheckForNewPlaceholders.isEmpty()) {
                this.checkForNewPlaceholders();
            }
        }
    }

    @Override
    protected void postSuccessfulReplacement(Indexable node) {
        this.toCheckForNewPlaceholders.add(node);
    }

    private void checkForNewPlaceholders() {
        for (final Indexable indexable : this.toCheckForNewPlaceholders) {
            BlixxKeyedNodeSpec.findNewPlaceholders(indexable, this.rootNode.getSpec().getBlixx().parserConfig().placeholderFormats(), (match, $) -> {
                final String placeholderKey = match.substring(1, match.length() - 1);
                this.placeholderToNode.computeIfAbsent(placeholderKey, ($2) -> new IndexedPlaceholder()).addIndexable(match, indexable);
            });
        }
    }

    private Map<String, IndexedPlaceholder> collectInitialPlaceholders() {
        final Iterator<BlixxNodeImpl> iterator = this.rootNode.iterator(true);
        final Map<String, IndexedPlaceholder> placeholderToNode = new HashMap<>();

        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            ((BlixxKeyedNodeSpec) node.getSpec()).collectPlaceholders(node, placeholderToNode);
        }

        return placeholderToNode;
    }
}
