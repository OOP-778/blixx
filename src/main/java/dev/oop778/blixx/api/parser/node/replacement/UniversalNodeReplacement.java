package dev.oop778.blixx.api.parser.node.replacement;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.BlixxTagHolder;
import dev.oop778.blixx.api.parser.node.kind.BlixxPlaceholderNode;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.Pair;
import java.util.*;
import java.util.function.BiConsumer;

public class UniversalNodeReplacement extends AbstractNodeReplacement {
    private Map<String, Set<Indexable<?>>> toRevisit = new LinkedHashMap<>();
    private Map<String, Set<Indexable<?>>> indexedNodesMap;

    public UniversalNodeReplacement(
            BlixxNodeInternal startingNode,
            Iterable<? extends BlixxPlaceholder<?>> placeholders,
            PlaceholderContext context) {
        super(startingNode, placeholders, context);
        this.indexedNodesMap = new LinkedHashMap<>();

        this.visitNodeForPlaceholders(startingNode, (placeholder, indexable) -> this.indexedNodesMap
                .computeIfAbsent(placeholder, ($) -> Collections.newSetFromMap(new IdentityHashMap<>()))
                .add(indexable));
    }

    @Override
    public void work() {
        while (!this.indexedNodesMap.isEmpty()) {
            for (final Map.Entry<String, Set<Indexable<?>>> entry : this.indexedNodesMap.entrySet()) {
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

            this.indexedNodesMap = this.toRevisit;
            this.toRevisit = new LinkedHashMap<>();
        }
    }

    @Override
    protected void postNewNode(BlixxNodeInternal node) {
        this.visitNodeForPlaceholders(node, (placeholder, indexable) -> this.toRevisit
                .computeIfAbsent(placeholder, ($) -> Collections.newSetFromMap(new IdentityHashMap<>()))
                .add(indexable));
    }

    public void visitNodeForPlaceholders(
            BlixxNodeInternal start, BiConsumer<String, Indexable<?>> placeholderAndNodeConsumer) {
        final Iterator<BlixxNodeInternal> iterator = start.iterator(true);

        while (iterator.hasNext()) {
            final BlixxNodeInternal node = iterator.next();
            if (node instanceof BlixxPlaceholderNode) {
                placeholderAndNodeConsumer.accept(((BlixxPlaceholderNode) node).getPlaceholder(), (Indexable<?>) node);
            }

            if (node instanceof BlixxTagHolder && ((BlixxTagHolder) node).hasIndexableTagData()) {
                for (final BlixxTag.WithDefinedData<?> tag : ((BlixxTagHolder) node).getTags()) {
                    if (tag.getDefinedData() instanceof Indexable.WithNodeContent) {
                        this.visitNodeForPlaceholders(
                                (BlixxNodeInternal) ((Indexable.WithNodeContent) tag.getDefinedData()).getNode(),
                                placeholderAndNodeConsumer);
                    }

                    if (tag.getDefinedData() instanceof Indexable.WithStringContent) {
                        this.tryIndexStringContent(
                                (Indexable.WithStringContent) tag.getDefinedData(), placeholderAndNodeConsumer);
                    }
                }
            }
        }
    }

    public void tryIndexStringContent(
            Indexable.WithStringContent<?> indexable, BiConsumer<String, Indexable<?>> placeholderAndNodeConsumer) {
        final String content = indexable.getContent();
        if (content.isEmpty()) {
            return;
        }

        for (final Pair<Character, Character> placeholderFormat :
                this.rootNode.blixx().parserConfig().placeholderFormats()) {
            final char startChar = placeholderFormat.getLeft();
            final char endChar = placeholderFormat.getRight();

            int pos = 0;
            while (pos < content.length()) {
                final int startIndex = content.indexOf(startChar, pos);
                if (startIndex == -1) {
                    break;
                }

                final int endIndex = content.indexOf(endChar, startIndex + 1);
                if (endIndex == -1) {
                    break;
                }

                final String fullPlaceholder = content.substring(startIndex + 1, endIndex);
                if (!fullPlaceholder.isEmpty()) {
                    placeholderAndNodeConsumer.accept(fullPlaceholder, indexable);
                }

                pos = endIndex + 1;
            }
        }
    }
}
