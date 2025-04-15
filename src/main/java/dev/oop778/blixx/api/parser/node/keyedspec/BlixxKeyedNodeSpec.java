package dev.oop778.blixx.api.parser.node.keyedspec;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.indexable.IndexableKey;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.parser.node.BlixxNodeSpec;
import dev.oop778.blixx.api.parser.node.IndexedPlaceholder;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Getter
public class BlixxKeyedNodeSpec implements BlixxNodeSpec {
    private final Blixx blixx;
    private final String input;
    private final Object parserKey;
    private final Map<Object, List<String>> indexableToPlaceholders = new HashMap<>();
    private int indexableIndexCounter;
    private static final Pattern PLACEHOLDER_INSIDE_PATTERN = Pattern.compile("[a-zA-Z_0-9]+");

    public static void findNewPlaceholders(Indexable indexable, List<Pair<Character, Character>> placeholderFormats, BiConsumer<String, Indexable> collector) {
        final String content;
        if (indexable instanceof Indexable.WithStringContent) {
            content = ((Indexable.WithStringContent) indexable).getContent();
        } else {
            content = ((Indexable.WithNodeContent) indexable).getNode().getContent();
        }

        if (content.isEmpty()) {
            return;
        }

        for (final Pair<Character, Character> placeholderFormat : placeholderFormats) {
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

                final String fullPlaceholder = content.substring(startIndex, endIndex + 1);
                if (!fullPlaceholder.isEmpty()) {
                    collector.accept(fullPlaceholder, indexable);
                }

                pos = endIndex + 1;
            }
        }
    }

    @Override
    public void indexPlaceholders(Indexable indexable, Blixx blixx) {
        final Indexable unwrapped = this.unwrap(indexable);
        findNewPlaceholders(indexable, blixx.parserConfig().placeholderFormats(), (placeholder, $) -> {
            this.indexableToPlaceholders.computeIfAbsent(indexable.getKey(), $1 -> new ArrayList<>(2)).add(placeholder);
        });

        if (unwrapped instanceof BlixxNode) {
            this.indexNodeTags((BlixxNode) unwrapped, blixx);
        }
    }

    @Override
    public boolean hasPlaceholders(Indexable node, boolean withTags) {
        final Indexable unwrapped = this.unwrap(node);
        final boolean result = this.indexableToPlaceholders.containsKey(node.getKey());
        if (result || !(unwrapped instanceof BlixxNode) || !((BlixxNode) unwrapped).isHasIndexableTagData()) {
            return true;
        }

        if (!withTags) {
            return false;
        }

        for (final BlixxTag.WithDefinedData<?> tag : ((BlixxNode) unwrapped).getTags()) {
            final Object definedData = tag.getDefinedData();
            if (!(definedData instanceof Indexable)) {
                continue;
            }

            if (this.indexableToPlaceholders.containsKey(((Indexable) definedData).getKey())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public BlixxNode createNode() {
        return new BlixxKeyedNodeImpl(this.createNodeKey(), this);
    }

    @Override
    public @UnknownNullability Object createNodeKey() {
        return new IndexableKey(this.indexableIndexCounter++, this.parserKey);
    }

    public void indexNodeTags(BlixxNode node, Blixx blixx) {
        final Iterable<BlixxTag.WithDefinedData<?>> tags = node.getTags();
        if (tags == null || !node.isHasIndexableTagData()) {
            return;
        }

        for (final BlixxTag.WithDefinedData<?> tag : tags) {
            final Object definedData = tag.getDefinedData();
            if (!(definedData instanceof Indexable)) {
                continue;
            }

            this.indexPlaceholders((Indexable) definedData, blixx);
        }
    }

    public void collectPlaceholders(BlixxNodeImpl node, Map<String, IndexedPlaceholder> placeholders) {
        // Collect node placeholders itself
        this.collectNodePlaceholders(node, placeholders);

        // Collect tag placeholders
        this.collectTagPlaceholders(node, placeholders);
    }

    private void collectNodePlaceholders(BlixxNodeImpl node, Map<String, IndexedPlaceholder> placeholders) {
        final List<String> keys = this.indexableToPlaceholders.get(node.getKey());
        if (keys == null) {
            return;
        }

        for (final String key : keys) {
            final String placeholder = key.substring(1, key.length() - 1);
            placeholders.computeIfAbsent(placeholder, $ -> new IndexedPlaceholder()).addIndexable(key, node);
        }
    }

    private void collectTagPlaceholders(BlixxNodeImpl node, Map<String, IndexedPlaceholder> placeholders) {
        final Iterable<BlixxTag.WithDefinedData<?>> tags = node.getTags();
        if (tags == null) {
            return;
        }

        if (!node.isHasIndexableTagData()) {
            return;
        }

        for (final BlixxTag.WithDefinedData<?> tag : tags) {
            final Object definedData = tag.getDefinedData();
            if (!(definedData instanceof Indexable)) {
                continue;
            }

            final List<String> strings = this.indexableToPlaceholders.get(((Indexable) definedData).getKey());
            if (strings != null) {
                for (final String key : strings) {
                    final String placeholder = key.substring(1, key.length() - 1);
                    placeholders.computeIfAbsent(placeholder, $ -> new IndexedPlaceholder()).addIndexable(key, ((Indexable) definedData));
                }
            }
        }
    }
}
