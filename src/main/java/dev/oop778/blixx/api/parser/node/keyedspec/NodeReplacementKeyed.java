package dev.oop778.blixx.api.parser.node.keyedspec;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.parser.node.replacement.AbstractNodeReplacement;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;

import java.util.*;

public class NodeReplacementKeyed extends AbstractNodeReplacement {
    private final Map<String, List<Indexable>> placeholderToNode;
    private final Set<Indexable> toCheckForNewPlaceholders;

    public NodeReplacementKeyed(BlixxNodeImpl node, Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        super(node, placeholders, context);
        this.placeholderToNode = this.collectPlaceholders();
        this.toCheckForNewPlaceholders = new HashSet<>();
    }

    public void work() {
        while (!this.placeholderToNode.isEmpty()) {
            for (final Map.Entry<String, List<Indexable>> entry : this.placeholderToNode.entrySet()) {
                final String fullPlaceholder = entry.getKey();

                for (final BlixxPlaceholder<?> blixxPlaceholder : this.placeholders) {
                    if (blixxPlaceholder instanceof BlixxPlaceholder.Literal) {
                        this.handleLiteralReplacement(fullPlaceholder, entry.getValue(), (BlixxPlaceholder.Literal<?>) blixxPlaceholder, this.context);
                        continue;
                    }

                    if (blixxPlaceholder instanceof BlixxPlaceholder.Pattern) {
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

    @Override
    protected void postSuccessfulReplacement(Indexable node) {
        this.toCheckForNewPlaceholders.add(node);
    }

    private void checkForNewPlaceholders() {
        for (final Indexable indexable : this.toCheckForNewPlaceholders) {
            BlixxKeyedNodeSpec.findNewPlaceholders(indexable, this.rootNode.getSpec().getBlixx().parserConfig().placeholderFormats(), (placeholder, $) -> this.placeholderToNode.computeIfAbsent(placeholder, ($2) -> new ArrayList<>()).add(indexable));
        }
    }

    private Map<String, List<Indexable>> collectPlaceholders() {
        final Iterator<BlixxNodeImpl> iterator = this.rootNode.iterator(true);
        final Map<String, List<Indexable>> placeholderToNode = new HashMap<>();

        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            ((BlixxKeyedNodeSpec) node.getSpec()).collectPlaceholders(node, placeholderToNode);
        }

        return placeholderToNode;
    }
}
