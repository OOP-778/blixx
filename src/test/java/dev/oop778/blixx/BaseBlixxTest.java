package dev.oop778.blixx;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.tag.BlixxTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseBlixxTest {
    protected static final Blixx BLIXX = Blixx.builder()
            .withStandardParserConfig((configurator) -> configurator
                    .withTags(BlixxTags.STANDARD)
                    .withPlaceholderFormat('%', '%')
                    .withPlaceholderFormat('{', '}')
                    .withPlaceholderFormat('<', '>')
            )
            .withStandardPlaceholderConfig()
            .build();

    protected void compareComponents(Component expected, Component given) {
        if (Objects.equals(expected, given)) {
            return;
        }

        final String expectedString = LegacyComponentSerializer.legacyAmpersand().serialize(expected);
        final String givenString = LegacyComponentSerializer.legacyAmpersand().serialize(given);

        assertEquals(expectedString, givenString);
    }
}
