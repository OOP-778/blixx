package dev.oop778.blixx.api.parser;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.config.ParserConfig;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class BlixxRootNodeImpl extends BlixxNodeImpl {
    private final String input;
    private final Map<String, List<BlixxNodeImpl>> placeholderToNode = new HashMap<>();
    private final Map<BlixxNodeImpl, List<String>> nodeToPlaceholder = new IdentityHashMap<>();

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

        boolean containsPlaceholder = false;
        for (final Character placeholderCharacter : parserConfig.placeholderCharacters()) {
            if (content.indexOf(placeholderCharacter) != -1) {
                containsPlaceholder = true;
                break;
            }
        }

        if (!containsPlaceholder) {
            return;
        }

        for (final Pattern placeholder : parserConfig.placeholderPatterns()) {
            final Matcher matcher = placeholder.matcher(content);
            while (matcher.find()) {
                this.placeholderToNode.computeIfAbsent(matcher.group(1), $ -> new ArrayList<>(2)).add(node);
                this.nodeToPlaceholder.computeIfAbsent(node, $ -> new ArrayList<>(2)).add(matcher.group(1));
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

                    final Object o = blixxPlaceholder.get(PlaceholderContext.create());
                    for (final BlixxNodeImpl node : entry.getValue()) {
                        node.setContent(node.getContent().replace(placeholder, o.toString()));
                    }
                }
            }
        }
    }

    public boolean hasPlaceholders(BlixxNodeImpl next) {
        return this.nodeToPlaceholder.containsKey(next);
    }
}
