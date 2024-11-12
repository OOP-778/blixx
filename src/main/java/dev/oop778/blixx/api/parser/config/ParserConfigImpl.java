package dev.oop778.blixx.api.parser.config;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.Pair;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApiStatus.Internal
@Getter
@Accessors(fluent = true)
public class ParserConfigImpl implements ParserConfig {
    private final Map<String, BlixxTag<?>> tags;
    private final List<Pair<Character, Character>> placeholderFormats;
    private final char tagOpen;
    private final char tagClose;
    private final List<Pattern> placeholderPatterns;
    private final List<Character> placeholderCharacters;
    private final Iterable<BlixxTag.Pattern<?>> patternTags;
    private final List<BlixxPlaceholder<String>> parsePlaceholders;
    private final Map<BlixxTag<?>, List<String>> tagNames;
    private final boolean useKeyBasedIndexing;

    public ParserConfigImpl(Map<String, BlixxTag<?>> tags, List<Pair<Character, Character>> placeholderFormats, List<BlixxPlaceholder<String>> parsePlaceholders, char tagOpen, char tagClose, boolean useKeyBasedIndexing) {
        this.tags = tags;
        this.placeholderFormats = placeholderFormats;
        this.parsePlaceholders = parsePlaceholders;
        this.tagOpen = tagOpen;
        this.tagClose = tagClose;
        this.useKeyBasedIndexing = useKeyBasedIndexing;
        this.placeholderPatterns = this.initPatterns();
        this.placeholderCharacters = new ArrayList<>(this.placeholderFormats.size() * 2);

        for (final Pair<Character, Character> placeholderFormat : this.placeholderFormats) {
            this.placeholderCharacters.add(placeholderFormat.getLeft());
            if (placeholderFormat.getRight() != placeholderFormat.getLeft()) {
                this.placeholderCharacters.add(placeholderFormat.getRight());
            }
        }

        this.patternTags = this.tags.values().stream()
                .filter(tag -> tag instanceof BlixxTag.Pattern)
                .map(tag -> (BlixxTag.Pattern<?>) tag)
                .collect(Collectors.toList());

        this.tagNames = new HashMap<>();
        for (final Map.Entry<String, BlixxTag<?>> entry : this.tags.entrySet()) {
            this.tagNames.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }
    }

    private List<Pattern> initPatterns() {
        final List<Pattern> result = new ArrayList<>(this.placeholderFormats.size());
        for (final Pair<Character, Character> placeholderFormat : this.placeholderFormats) {
            result.add(Pattern.compile(Pattern.quote(placeholderFormat.getLeft() + "") + "([a-zA-Z_0-9]+)" + Pattern.quote(placeholderFormat.getRight() + ""), Pattern.MULTILINE & Pattern.CASE_INSENSITIVE));
        }

        return result;
    }
}
