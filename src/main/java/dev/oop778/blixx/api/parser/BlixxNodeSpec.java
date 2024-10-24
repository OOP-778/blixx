package dev.oop778.blixx.api.parser;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.config.ParserConfig;
import dev.oop778.blixx.api.util.Pair;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.regex.Pattern;

// This stores relevant information about the parsing (placeholders, original input, any extra information)
// The class is immutable once parsed it'll never change again
// TODO: There's a little problem with storing nodes here, since if node content gets replaced or anything, it'll no longer match the hashcode
// I think best solution is to store index of node creation
@RequiredArgsConstructor
public class BlixxNodeSpec {
    private final String input;
    private final Map<String, List<NodeKey>> placeholderToNode = new HashMap<>();
    private final Map<NodeKey, List<String>> nodeToPlaceholder = new HashMap<>();

    private static final Pattern PLACEHOLDER_INSIDE_PATTERN = Pattern.compile("[a-zA-Z_0-9]+");

    public void indexPlaceholdersOf(BlixxNodeImpl node, Blixx blixx) {
        final ParserConfig parserConfig = blixx.parserConfig();
        final String content = node.getContent();
        if (node.getContent().isEmpty()) {
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
                    this.placeholderToNode.computeIfAbsent(fullPlaceholder, $ -> new ArrayList<>(2)).add(node.getKey());
                    this.nodeToPlaceholder.computeIfAbsent(node.getKey(), $ -> new ArrayList<>(2)).add(fullPlaceholder);
                }

                pos = endIndex + 1;
            }
        }
    }

    public boolean hasPlaceholders(BlixxNodeImpl node) {
        return this.nodeToPlaceholder.containsKey(node.getKey());
    }

    public List<String> getPlaceholders(BlixxNodeImpl node) {
        return this.nodeToPlaceholder.get(node.getKey());
    }
}
