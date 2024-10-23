package dev.oop778.blixx.api.parser.config;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.api.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface ParserConfig {
    boolean supportsLegacyColorCodes();
    boolean supportsHexColorCodes();
    Map<String, BlixxTag<?>> tags();
    Map<BlixxTag<?>, List<String>> tagNames();
    char tagOpen();
    char tagClose();
    boolean strictMode();
    List<Pair<Character, Character>> placeholderFormats();
    List<Pattern> placeholderPatterns();
    List<Character> placeholderCharacters();
    Iterable<BlixxTag.Pattern<?>> patternTags();
    List<BlixxPlaceholder<String>> parsePlaceholders();
}
