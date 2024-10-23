package dev.oop778.blixx;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.tag.BlixxTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        final Blixx build = Blixx.builder()
                .withStandardParserConfig((configurator) -> configurator
                        .withTags(BlixxTags.DEFAULT_TAGS)
                        .withPlaceholderFormat('%', '%')
                        .withPlaceholderFormat('{', '}')
                        .withParsePlaceholder(BlixxPlaceholder.<String>builder()
                                .contextual()
                                .withExact(ColorScheme.class)
                                .pattern()
                                .withPattern(Pattern.compile("\\{([a-z]+)_color(?:_([1-9]))?}"))
                                .withContextAndMatcherSupplying((context, matcher) -> {
                                    // main color
                                    if (matcher.group(2) == null) {
                                        return context.getHexColor(matcher.group(1), 0);
                                    }

                                    // Shaded color
                                    return context.getHexColor(matcher.group(1), Integer.parseInt(matcher.group(2)));
                                })
                                .build()))
                .withStandardPlaceholderConfig()
                .build();

        /*
        example:
        <gradient:red:green><bold>Hello world
        processors pipeline:
        - build style using Decorators
        - build component builder using Component Visitor
        */
        final BlixxComponent parse = build.parse("<bold><gradient:red:green>Hello world");
        System.out.println(LegacyComponentSerializer.legacyAmpersand().serialize(parse.asComponent()));
    }

    interface ColorScheme {
        String getHexColor(String colorName, int shade);
    }

    public static class ColorSchemeImpl implements ColorScheme {
        // Map of color names to another map of shade index to hex color
        private final Map<String, Map<Integer, String>> colorMap;

        public ColorSchemeImpl() {
            this.colorMap = new HashMap<>();
            this.addColorWithShades("primary", "#FF0000");
            this.addColorWithShades("secondary", "#0000FF");
        }

        @Override
        public String getHexColor(String colorName, int shade) {
            final Map<Integer, String> shades = this.colorMap.get(colorName);
            if (shades == null || !shades.containsKey(shade)) {
                throw new IllegalArgumentException("Color or shade not found");
            }

            return shades.get(shade);
        }

        // Adds a color with default shades (0 to 9)
        private void addColorWithShades(String colorName, String defaultHex) {
            final Map<Integer, String> shadesMap = new HashMap<>();
            shadesMap.put(0, defaultHex); // Default color at index 0

            // Generate shades for indices 1 to 9 (lighter or darker versions)
            for (int i = 1; i <= 9; i++) {
                shadesMap.put(i, this.generateShade(defaultHex, i));
            }

            this.colorMap.put(colorName, shadesMap);
        }

        // Method to generate a shade from a hex color (simplified logic)
        private String generateShade(String hexColor, int shadeIndex) {
            // Here you would implement logic to lighten or darken the color based on the index
            // This is just an example logic where we modify the hex string slightly for demonstration
            int red = Integer.parseInt(hexColor.substring(1, 3), 16);
            int green = Integer.parseInt(hexColor.substring(3, 5), 16);
            int blue = Integer.parseInt(hexColor.substring(5, 7), 16);

            // Adjust color components (simplified darkening)
            red = Math.max(0, red - shadeIndex * 10);
            green = Math.max(0, green - shadeIndex * 10);
            blue = Math.max(0, blue - shadeIndex * 10);

            return String.format("#%02X%02X%02X", red, green, blue);
        }
    }

}
