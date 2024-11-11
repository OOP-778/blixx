package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import lombok.NonNull;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public class ColorTag implements BlixxTag.Pattern<TextColor>, ColorChangingTag<TextColor> {
    public static final ColorTag INSTANCE = new ColorTag();
    public static final ColorTag.Processor PROCESSOR = new ColorTag.Processor();
    public static final java.util.regex.Pattern HEX_PATTERN = java.util.regex.Pattern.compile("#([0-9a-fA-F]{6})");

    protected ColorTag() {}

    public static TextColor decode(String input) {
        final TextColor namedTextColor = NamedTextColor.NAMES.value(input);
        if (namedTextColor != null) {
            return namedTextColor;
        }

        // try matching based on hex
        final String matchedHex = extractHexColorCode(input);
        if (matchedHex != null) {
            return hexToColor(matchedHex);
        }

        return null;
    }

    private static String extractHexColorCode(String input) {
        if (input.charAt(0) == '#') {
            for (int i = 1; i < 7; i++) {
                final char c = input.charAt(i);
                if (!isHexCharacterBitwise(c)) {
                    return null;
                }
            }

            return input.substring(1);
        }

        return null;
    }

    private static boolean isHexCharacterBitwise(char c) {
        return (c >= '0' && c <= '9') || ((c | 0x20) >= 'a' && (c | 0x20) <= 'f');
        // `c | 0x20` converts uppercase letters to lowercase
    }

    public static TextColor decodeOrThrow(String input) {
        final TextColor color = decode(input);
        if (color == null) {
            throw new IllegalArgumentException("Invalid color: " + input);
        }

        return color;
    }

    public static TextColor hexToColor(String hex) {
        if (hex.length() == 3) {
            hex = String.valueOf(hex.charAt(0)) + hex.charAt(0) +
                    hex.charAt(1) + hex.charAt(1) +
                    hex.charAt(2) + hex.charAt(2);
        }

        final int color = Integer.parseInt(hex, 16);
        return TextColor.color(color);
    }

    @Override
    public boolean canCoexist(@NonNull BlixxProcessor.Context context, BlixxTag<?> other) {
        return !other.isInstanceOf(ColorChangingTag.class);
    }

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    @Override
    public TextColor createData(@NonNull BlixxProcessor.@NonNull ParserContext context, @NotNull BaseArgumentQueue args) {
        return decode(args.pop());
    }

    @Override
    public java.util.regex.Pattern getPattern() {
        return HEX_PATTERN;
    }

    @Override
    public TextColor createDataOfMatcher(BlixxProcessor.@NonNull ParserContext context, @NotNull Matcher matcher) {
        final String group = matcher.group(1);
        return hexToColor(group);
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<TextColor> {
        @Override
        public void decorate(@NonNull ComponentContext context) {
            context.getStyleBuilder().color(context.getData());
        }
    }
}
