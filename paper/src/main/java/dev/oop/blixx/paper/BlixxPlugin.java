package dev.oop.blixx.paper;

import dev.oop.blixx.paper.command.SimpleCommands;
import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BlixxPlugin extends JavaPlugin {
    private final Blixx blixx = Blixx.builder()
            .withStandardParserConfig((configurator) -> configurator
                    .withTags(BlixxTags.DEFAULT_TAGS)
                    .withPlaceholderFormat('%', '%')
                    .withPlaceholderFormat('{', '}')
                    .withPlaceholderFormat('<', '>')
                    .withParsePlaceholder(BlixxPlaceholder.<String>builder()
                            .contextual()
                            .withExact(ColorScheme.class)
                            .pattern()
                            .withPattern(Pattern.compile("\\{([a-z]+)_color(?:_([1-9]))?}"))
                            .withContextAndMatcherSupplying((context, matcher) -> {
                                // main color
                                if (matcher.group(2) == null) {
                                    return "<" + context.getHexColor(matcher.group(1), 0) + ">";
                                }

                                // Shaded color
                                return "<" + context.getHexColor(matcher.group(1), Integer.parseInt(matcher.group(2))) + ">";
                            })
                            .build()))
            .withStandardPlaceholderConfig()
            .build();
    private static final ColorSchemeImpl colorScheme = new ColorSchemeImpl();

    @Override
    public void onEnable() {
        SimpleCommands.registerCommand((command) -> command
                .async(true)
                .alias("parseBlixxInput")
                .command((sender, arguments) -> {
                    final Map<String, String> placeholders = new HashMap<>();
                    final StringBuilder input = new StringBuilder();

                    while (arguments.hasNext()) {
                        final String next = arguments.next();
                        if (!next.startsWith("--p")) {
                            input.append(next);
                            if (arguments.hasNext()) {
                                input.append(' ');
                            }
                            continue;
                        }

                        final String placeholder = arguments.next();
                        final String value = arguments.next();
                        placeholders.put(placeholder, value);
                    }

                    final BlixxComponent parse = this.blixx.parse(input.toString(), PlaceholderContext.create(colorScheme));
                    final BlixxComponent blixxComponent = parse.replace(this.parseBlixxPlaceholders(placeholders), null);

                    final Component component = blixxComponent.asComponent();
                    sender.sendMessage(component);

                    final Component miniMessageComponent = MiniMessage.miniMessage().deserialize(input.toString(), this.parseMiniMessagePlaceholders(placeholders));
                    sender.sendMessage(miniMessageComponent);
                })
        );
    }

    private List<BlixxPlaceholder<?>> parseBlixxPlaceholders(Map<String, String> placeholders) {
        final List<BlixxPlaceholder<?>> result = new ArrayList<>();
        for (final Map.Entry<String, String> stringStringEntry : placeholders.entrySet()) {
            final String key = stringStringEntry.getKey();
            final String value = stringStringEntry.getValue();
            if (value.startsWith("#")) {
                final String substring = value.substring(1);
                result.add(BlixxPlaceholder.literal(key, this.blixx.parse(substring)));
                continue;
            }

            result.add(BlixxPlaceholder.literal(key, value));
        }

        return result;
    }

    private TagResolver[] parseMiniMessagePlaceholders(Map<String, String> placeholders) {
        final List<TagResolver> result = new ArrayList<>();
        for (final Map.Entry<String, String> stringStringEntry : placeholders.entrySet()) {
            final String key = stringStringEntry.getKey();
            final String value = stringStringEntry.getValue();
            if (value.startsWith("#")) {
                final String substring = value.substring(1);
                result.add(Placeholder.component(key, MiniMessage.miniMessage().deserialize(substring)));
                continue;
            }

            result.add(Placeholder.parsed(key, value));
        }

        return result.toArray(TagResolver[]::new);
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
