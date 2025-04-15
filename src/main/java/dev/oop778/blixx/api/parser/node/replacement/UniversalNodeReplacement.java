package dev.oop778.blixx.api.parser.node.replacement;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.parser.node.IndexedPlaceholder;
import dev.oop778.blixx.api.parser.node.keyedspec.BlixxKeyedNodeSpec;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;

import java.util.*;

public class UniversalNodeReplacement extends AbstractNodeReplacement {
    private final Set<Indexable> toRevisit;
    private final Map<String, IndexedPlaceholder> indexedPlaceholderMap;
    private final Blixx blixx;

    public UniversalNodeReplacement(BlixxNodeImpl rootNode, Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        super(rootNode, placeholders, context);
        this.indexedPlaceholderMap = this.collectInitialPlaceholders();
        this.toRevisit = new HashSet<>();
        this.blixx = rootNode.getSpec() == null ? Blixx.standard() : rootNode.getSpec().getBlixx();
    }

    @Override
    public void work() {
        while (!this.indexedPlaceholderMap.isEmpty()) {
            for (final Map.Entry<String, IndexedPlaceholder> entry : this.indexedPlaceholderMap.entrySet()) {
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

            this.indexedPlaceholderMap.clear();
            if (!this.toRevisit.isEmpty()) {
                this.collectNewPlaceholders();
            }
        }
    }

    @Override
    protected void postSuccessfulReplacement(Indexable indexable) {
        this.toRevisit.add(indexable);
    }

    private void collectNewPlaceholders() {
        for (final Indexable next : this.toRevisit) {
            BlixxKeyedNodeSpec.findNewPlaceholders(next, this.blixx.parserConfig().placeholderFormats(), (placeholder, $) -> {
                final String substring = placeholder.substring(1, placeholder.length() - 1);
                this.indexedPlaceholderMap.computeIfAbsent(substring, ($2) -> new IndexedPlaceholder()).addIndexable(placeholder, next);
            });
        }

        this.toRevisit.clear();
    }

    private Map<String, IndexedPlaceholder> collectInitialPlaceholders() {
        final Iterator<BlixxNodeImpl> iterator = this.rootNode.iterator(true);
        final Map<String, IndexedPlaceholder> result = new LinkedHashMap<>();

        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            node.collectSelfPlaceholders(result);
        }

        return result;
    }
}
