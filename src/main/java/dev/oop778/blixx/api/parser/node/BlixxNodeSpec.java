package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.config.ParserConfig;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.indexable.IndexableKey;
import dev.oop778.blixx.api.placeholder.PlaceholderConfig;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.ObjectArray;
import dev.oop778.blixx.util.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// This stores relevant information about the parsing (placeholders, original input, any extra information)
// The class is immutable once parsed it'll never change again
// TODO: There's a little problem with storing nodes here, since if node content gets replaced or anything, it'll no longer match the hashcode
// I think best solution is to store index of node creation
@RequiredArgsConstructor
@Getter
public class BlixxNodeSpec {
    private final Blixx blixx;
    private final String input;
    private final Object parserKey;
    private final Map<IndexableKey, List<String>> indexableToPlaceholders = new HashMap<>();
    private int indexableIndexCounter;
    private static final Pattern PLACEHOLDER_INSIDE_PATTERN = Pattern.compile("[a-zA-Z_0-9]+");

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

            final Indexable indexable = (Indexable) definedData;
            if (indexable instanceof Indexable.WithStringContent) {
                final Indexable.WithStringContent indexableWithContent = (Indexable.WithStringContent) indexable;
                this.indexPlaceholders(indexableWithContent.getKey(), indexableWithContent.getContent(), blixx);
            }

            if (indexable instanceof Indexable.WithNodeContent) {
                final Indexable.WithNodeContent indexableWithContent = (Indexable.WithNodeContent) indexable;
                this.indexPlaceholdersOf(indexableWithContent.getNode(), blixx);
            }
        }
    }

    public void indexPlaceholdersOf(BlixxNode node, Blixx blixx) {
        this.indexPlaceholders(node.getKey(), node.getContent(), blixx);
        this.indexNodeTags(node, blixx);
    }

    public int getNextIndex() {
        return this.indexableIndexCounter++;
    }

    public boolean hasPlaceholders(BlixxNodeImpl node) {
        final boolean result = this.indexableToPlaceholders.containsKey(node.getKey());
        if (result || !node.isHasIndexableTagData()) {
            return true;
        }

        for (final BlixxTag.WithDefinedData<?> tag : node.getTags()) {
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

    public void collectPlaceholders(BlixxNodeImpl node, Map<String, List<Indexable>> placeholders) {
        // Collect node placeholders itself
        this.collectNodePlaceholders(node, placeholders);

        // Collect tag placeholders
        this.collectTagPlaceholders(node, placeholders);
    }

    private void collectNodePlaceholders(BlixxNodeImpl node, Map<String, List<Indexable>> placeholders) {
        final List<String> keys = this.indexableToPlaceholders.get(node.getKey());
        if (keys == null) {
            return;
        }

        for (final String key : keys) {
            placeholders.computeIfAbsent(key, $ -> new ArrayList<>(2)).add(node);
        }
    }

    private void collectTagPlaceholders(BlixxNodeImpl node, Map<String, List<Indexable>> placeholders) {
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
                for (final String placeholder : strings) {
                    placeholders.computeIfAbsent(placeholder, $ -> new ArrayList<>(2)).add((Indexable) definedData);
                }
            }
        }
    }

    public static boolean findAnyPlaceholders(Indexable indexable, List<Pair<Character, Character>> placeholderFormats) {
        final String content;
        if (indexable instanceof Indexable.WithStringContent) {
            content = ((Indexable.WithStringContent) indexable).getContent();
        } else {
            content = ((Indexable.WithNodeContent) indexable).getNode().getContent();
        }

        if (content.isEmpty()) {
            return false;
        }

        for (final Pair<Character, Character> placeholderFormat : placeholderFormats) {
            final char start = placeholderFormat.getLeft();
            final char end = placeholderFormat.getRight();

            if (content.indexOf(start) != -1 && content.indexOf(end) != -1) {
                return true;
            }
        }

        return false;
    }

    public static void findNewPlaceholders(Indexable indexable, Map<String, List<Indexable>> into, List<Pair<Character, Character>> placeholderFormats) {
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
            final char start = placeholderFormat.getLeft();
            final char end = placeholderFormat.getRight();

            int pos = 0;
            while (pos < content.length()) {
                final int startIndex = content.indexOf(start, pos);
                if (startIndex == -1) {
                    break;
                }

                final int endIndex = content.indexOf(end, startIndex + 1);
                if (endIndex == -1) {
                    break;
                }

                final String fullPlaceholder = content.substring(startIndex, endIndex + 1);
                if (!fullPlaceholder.isEmpty() && PLACEHOLDER_INSIDE_PATTERN.matcher(fullPlaceholder.substring(1, fullPlaceholder.length() - 1)).matches()) {
                    into.computeIfAbsent(fullPlaceholder, $ -> new ArrayList<>(2)).add(indexable);
                }

                pos = endIndex + 1;
            }
        }
    }

    private void indexPlaceholders(IndexableKey key, String content, Blixx blixx) {
        final ParserConfig parserConfig = blixx.parserConfig();
        if (content.isEmpty()) {
            return;
        }

        for (final Pair<Character, Character> placeholderFormat : parserConfig.placeholderFormats()) {
            final char start = placeholderFormat.getLeft();
            final char end = placeholderFormat.getRight();

            int pos = 0;
            while (pos < content.length()) {
                final int startIndex = content.indexOf(start, pos);
                if (startIndex == -1) {
                    break;
                }

                final int endIndex = content.indexOf(end, startIndex + 1);
                if (endIndex == -1) {
                    break;
                }

                final String fullPlaceholder = content.substring(startIndex, endIndex + 1);
                if (!fullPlaceholder.isEmpty() && PLACEHOLDER_INSIDE_PATTERN.matcher(fullPlaceholder.substring(1, fullPlaceholder.length() - 1)).matches()) {
                    this.indexableToPlaceholders.computeIfAbsent(key, $ -> new ArrayList<>(2)).add(fullPlaceholder);
                }

                pos = endIndex + 1;
            }
        }
    }
}
