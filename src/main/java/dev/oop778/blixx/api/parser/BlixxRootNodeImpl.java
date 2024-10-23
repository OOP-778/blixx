package dev.oop778.blixx.api.parser;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.config.ParserConfig;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.util.Pair;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class BlixxRootNodeImpl extends BlixxNodeImpl {
    private final String input;
    private final Map<String, List<BlixxNodeImpl>> placeholderToNode = new HashMap<>();
    private final Map<BlixxNodeImpl, List<String>> nodeToPlaceholder = new IdentityHashMap<>();
    private static final Pattern PLACEHOLDER_INSIDE_PATTERN = Pattern.compile("[a-zA-Z_0-9]+");

    @Override
    public String toString() {
        return this.input;
    }

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

                final String placeholder = content.substring(startIndex + 1, endIndex);
                if (!placeholder.isEmpty() && PLACEHOLDER_INSIDE_PATTERN.matcher(placeholder).matches()) {
                    this.placeholderToNode.computeIfAbsent(placeholder, $ -> new ArrayList<>(2)).add(node);
                    this.nodeToPlaceholder.computeIfAbsent(node, $ -> new ArrayList<>(2)).add(placeholder);
                }

                pos = endIndex + 1;
            }
        }
    }

    public void replace(List<BlixxPlaceholder<?>> placeholders) {
        for (final Map.Entry<String, List<BlixxNodeImpl>> entry : this.placeholderToNode.entrySet()) {
            final String placeholder = entry.getKey();

            for (final BlixxPlaceholder<?> blixxPlaceholder : placeholders) {
                if (blixxPlaceholder instanceof BlixxPlaceholder.Literal<?>) {
                    final Collection<String> keys = ((BlixxPlaceholder.Literal<?>) blixxPlaceholder).keys();
                    if (!keys.contains(placeholder)) {
                        continue;
                    }

                    final Object value = blixxPlaceholder.get(PlaceholderContext.create());
                    for (final BlixxNodeImpl node : entry.getValue()) {
                        node.setContent(node.getContent().replace(placeholder, String.valueOf(value)));
                    }
                }
            }
        }
    }

    public boolean hasPlaceholders(BlixxNodeImpl next) {
        return this.nodeToPlaceholder.containsKey(next);
    }
}
