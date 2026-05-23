package dev.oop778.blixx.support;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyFormatSupport {
    private static final Map<Character, String> LEGACY_CODES_TO_TAGS;
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("§|&([0-9abcdefghlijmnpqstu])");

    static {
        LEGACY_CODES_TO_TAGS = new HashMap<>();
        LEGACY_CODES_TO_TAGS.put('0', "<black>");
        LEGACY_CODES_TO_TAGS.put('1', "<dark_blue>");
        LEGACY_CODES_TO_TAGS.put('2', "<dark_green>");
        LEGACY_CODES_TO_TAGS.put('3', "<dark_aqua>");
        LEGACY_CODES_TO_TAGS.put('4', "<dark_red>");
        LEGACY_CODES_TO_TAGS.put('5', "<dark_purple>");
        LEGACY_CODES_TO_TAGS.put('6', "<gold>");
        LEGACY_CODES_TO_TAGS.put('7', "<gray>");
        LEGACY_CODES_TO_TAGS.put('8', "<dark_gray>");
        LEGACY_CODES_TO_TAGS.put('9', "<blue>");
        LEGACY_CODES_TO_TAGS.put('a', "<green>");
        LEGACY_CODES_TO_TAGS.put('b', "<aqua>");
        LEGACY_CODES_TO_TAGS.put('c', "<red>");
        LEGACY_CODES_TO_TAGS.put('d', "<light_purple>");
        LEGACY_CODES_TO_TAGS.put('e', "<yellow>");
        LEGACY_CODES_TO_TAGS.put('f', "<white>");

        LEGACY_CODES_TO_TAGS.put('k', "<obf>");
        LEGACY_CODES_TO_TAGS.put('l', "<b>");
        LEGACY_CODES_TO_TAGS.put('m', "<st>");
        LEGACY_CODES_TO_TAGS.put('o', "<l>");
        LEGACY_CODES_TO_TAGS.put('r', "<reset>");
    }

    public static String preprocessInput(String input) {
        final StringBuffer sb = new StringBuffer();
        final Matcher matcher = LEGACY_COLOR_PATTERN.matcher(input);
        while (matcher.find()) {
            final char code = matcher.group(1).toCharArray()[0];
            final String tag = LEGACY_CODES_TO_TAGS.get(code);
            if (tag == null) {
                continue;
            }

            matcher.appendReplacement(sb, tag);
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
