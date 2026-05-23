package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.component.BlixxColor;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.StringQueue;
import java.util.regex.Matcher;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ColorTag implements BlixxTag.Pattern<BlixxColor>, ColorChangingTag<BlixxColor> {
    public static final ColorTag INSTANCE = new ColorTag();
    public static final ColorTag.Processor PROCESSOR = new ColorTag.Processor();
    public static final java.util.regex.Pattern HEX_PATTERN = java.util.regex.Pattern.compile("#([0-9a-fA-F]{6})");

    protected ColorTag() {}

    @Nullable
    public static BlixxColor decode(String input) {
        final BlixxColor namedColor = NamedColors.byName(input);
        if (namedColor != null) {
            return namedColor;
        }

        // try matching based on hex
        if (input.length() >= 4 && input.charAt(0) == '#') {
            final String hex = input.substring(1);
            if (isValidHex(hex)) {
                return BlixxColor.fromHex(hex);
            }
        }

        return null;
    }

    private static boolean isValidHex(String hex) {
        if (hex.length() != 6 && hex.length() != 3) {
            return false;
        }
        for (int i = 0; i < hex.length(); i++) {
            final char c = hex.charAt(i);
            if (!((c >= '0' && c <= '9') || ((c | 0x20) >= 'a' && (c | 0x20) <= 'f'))) {
                return false;
            }
        }
        return true;
    }

    public static BlixxColor decodeOrThrow(String input) {
        final BlixxColor color = decode(input);
        if (color == null) {
            throw new IllegalArgumentException("Invalid color: " + input);
        }

        return color;
    }

    @Override
    public boolean canCoexist(BlixxTag<?> other) {
        return !other.isInstanceOf(ColorChangingTag.class) && !(other.isInstanceOf(DecorationTag.class));
    }

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    @Override
    public BlixxColor createData(@NonNull BlixxProcessor.@NonNull ParserContext context, @NotNull StringQueue args) {
        return decode(args.pop());
    }

    @Override
    public java.util.regex.Pattern getPattern() {
        return HEX_PATTERN;
    }

    @Override
    public BlixxColor createDataOfMatcher(BlixxProcessor.@NonNull ParserContext context, @NotNull Matcher matcher) {
        final String group = matcher.group(1);
        return BlixxColor.fromHex(group);
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<BlixxColor> {
        @Override
        public void decorate(@NonNull ComponentContext context) {
            context.getStyle().color(context.getData());
        }
    }
}
