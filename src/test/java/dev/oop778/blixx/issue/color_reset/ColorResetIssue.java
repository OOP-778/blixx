package dev.oop778.blixx.issue.color_reset;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.ComponentDecoration;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.tag.BlixxTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorResetIssue {
    private static final Blixx INSTANCE = Blixx.builder()
            .withStandardParserConfig(config -> config
                    .withTags(BlixxTags.STANDARD)
                    .withPlaceholderFormat('%', '%')
                    .withPlaceholderFormat('{', '}')
                    .withPlaceholderFormat('<', '>')
                    .useKeyBasedPlaceholderIndexing()
            )
            .withStandardPlaceholderConfig()
            .build();

    public static void main(String[] args) {
        final BlixxComponent input = INSTANCE.parseComponent("<yellow><b>Boosters <dark_gray>:: <gray>Gave booster {color}{booster} <gray>with duration <white>{duration} <gray>to <white>{player}");
        final Component component = Replacer.createImmutable()
                .withLiteral("color", ComponentDecoration.of("<aqua>"))
                .withLiteral("booster", "Tokens with duration")
                .withLiteral("duration", "1m")
                .withLiteral("player", "OOP_778")
                .accept(input)
                .complete()
                .asComponent();

        System.out.println(LegacyComponentSerializer.legacyAmpersand().serialize(component));
    }
}
