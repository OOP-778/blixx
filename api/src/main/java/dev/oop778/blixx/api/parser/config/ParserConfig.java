package dev.oop778.blixx.api.parser.config;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Configuration for the Blixx parser: registered tags, placeholder formats, and legacy format support. */
public interface ParserConfig {
    /** Returns all registered tags keyed by name. */
    Map<String, BlixxTag<?>> tags();
    /** Returns a reverse mapping from tag to its registered names. */
    Map<BlixxTag<?>, List<String>> tagNames();
    /** Returns the tag opening character (default {@code <}). */
    char tagOpen();
    /** Returns the tag closing character (default {@code >}). */
    char tagClose();
    /** Returns the configured placeholder delimiter pairs (e.g., {@code {/}}, {@code %/%}). */
    List<Pair<Character, Character>> placeholderFormats();
    /** Returns compiled regex patterns for each placeholder format. */
    List<Pattern> placeholderPatterns();
    /** Returns the set of all characters used as placeholder delimiters. */
    Set<Character> placeholderCharacters();
    /** Returns all regex-pattern-based tags. */
    Iterable<BlixxTag.Pattern<?>> patternTags();
    /** Returns placeholders that are resolved at parse time (before node construction). */
    List<BlixxPlaceholder<String>> parsePlaceholders();
    /** Returns whether legacy {@code &}-color codes are supported. */
    boolean supportLegacyFormat();
}
