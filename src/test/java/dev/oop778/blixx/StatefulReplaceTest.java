package dev.oop778.blixx;

import static org.junit.jupiter.api.Assertions.*;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.BlixxTagHolder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.api.tag.BlixxTags;
import dev.oop778.blixx.tag.decoration.ClickTag;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StatefulReplaceTest {
    private static Blixx blixx;

    @BeforeAll
    static void setup() {
        blixx = Blixx.builder()
                .withStandardParserConfig(configurator -> configurator
                        .withTags(BlixxTags.STANDARD)
                        .withPlaceholderFormat('%', '%')
                        .withPlaceholderFormat('{', '}'))
                .withStandardPlaceholderConfig()
                .build();
    }

    private String getClickActionValue(BlixxComponent component) {
        BlixxNodeInternal node = (BlixxNodeInternal) component.getNode();
        Iterator<BlixxNodeInternal> it = node.iterator(true);
        while (it.hasNext()) {
            BlixxNodeInternal n = it.next();
            if (n instanceof BlixxTagHolder) {
                for (BlixxTag.WithDefinedData<?> tag : ((BlixxTagHolder) n).getTags()) {
                    if (tag.getDefinedData() instanceof ClickTag.Action) {
                        return ((ClickTag.Action) tag.getDefinedData()).getValue();
                    }
                }
            }
        }
        return null;
    }

    @Nested
    class ClickUrlPlaceholderReplacement {
        @Test
        void replacesPlaceholderInClickUrl() {
            BlixxComponent component = blixx.parseComponent("<click:open_url:\"{player}\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("player", "Steve")));

            assertEquals("Steve", getClickActionValue(replaced));
        }

        @Test
        void doesNotReplaceBareKeyWithoutDelimiters() {
            BlixxComponent component = blixx.parseComponent("<click:open_url:\"player\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("player", "Steve")));

            assertEquals("player", getClickActionValue(replaced));
        }

        @Test
        void replacesOnlyDelimitedOccurrence() {
            BlixxComponent component = blixx.parseComponent("<click:open_url:\"playerinfo-{player}\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("player", "Steve")));

            assertEquals("playerinfo-Steve", getClickActionValue(replaced));
        }

        @Test
        void replacesMultipleOccurrences() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"{player}-{player}\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("player", "Steve")));

            assertEquals("Steve-Steve", getClickActionValue(replaced));
        }

        @Test
        void replacesMultipleDifferentPlaceholders() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"{greeting}-{name}\">Click</click>");
            BlixxComponent replaced = component.replace(
                    List.of(BlixxPlaceholder.literal("greeting", "Hello"), BlixxPlaceholder.literal("name", "Steve")));

            assertEquals("Hello-Steve", getClickActionValue(replaced));
        }

        @Test
        void preservesUnmatchedPlaceholders() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"{player}-{unknown}\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("player", "Steve")));

            assertEquals("Steve-{unknown}", getClickActionValue(replaced));
        }

        @Test
        void keyAsSubstringOfAnotherWordNotReplaced() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"playername\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("player", "Steve")));

            assertEquals("playername", getClickActionValue(replaced));
        }

        @Test
        void adjacentPlaceholdersDifferentDelimiters() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"{a}{b}\">Click</click>");
            BlixxComponent replaced =
                    component.replace(List.of(BlixxPlaceholder.literal("a", "X"), BlixxPlaceholder.literal("b", "Y")));

            assertEquals("XY", getClickActionValue(replaced));
        }

        @Test
        void placeholderAtStartOfValue() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"{cmd} args\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("cmd", "say")));

            assertEquals("say args", getClickActionValue(replaced));
        }

        @Test
        void placeholderAtEndOfValue() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"run {cmd}\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("cmd", "help")));

            assertEquals("run help", getClickActionValue(replaced));
        }

        @Test
        void singleCharPlaceholderKey() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"{x}\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("x", "value")));

            assertEquals("value", getClickActionValue(replaced));
        }

        @Test
        void onlyDelimiterBoundaryTriggersReplacement() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"test\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("es", "XX")));

            assertEquals("test", getClickActionValue(replaced));
        }
    }

    @Nested
    class PercentFormatInStringContent {
        @Test
        void replacesPercentDelimitedPlaceholder() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"%player%\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("player", "Steve")));

            assertEquals("Steve", getClickActionValue(replaced));
        }

        @Test
        void replacesPercentPlaceholderWithSurroundingText() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"say %player%\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("player", "Steve")));

            assertEquals("say Steve", getClickActionValue(replaced));
        }

        @Test
        void adjacentSameCharDelimitersDoNotCrash() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"%a%%b%\">Click</click>");
            BlixxComponent replaced =
                    component.replace(List.of(BlixxPlaceholder.literal("a", "X"), BlixxPlaceholder.literal("b", "Y")));

            assertEquals("XY", getClickActionValue(replaced));
        }

        @Test
        void sharedDelimiterSecondKeyNotDoubleReplaced() {
            // In %player%player%, only the first %player% is a valid placeholder.
            // The trailing "player%" is literal text.
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"%player%player%\">Click</click>");
            BlixxComponent replaced = component.replace(List.of(BlixxPlaceholder.literal("player", "Steve")));

            assertEquals("Steveplayer%", getClickActionValue(replaced));
        }

        @Test
        void multiplePercentPlaceholdersSeparatedByText() {
            BlixxComponent component = blixx.parseComponent("<click:run_command:\"%a% and %b%\">Click</click>");
            BlixxComponent replaced =
                    component.replace(List.of(BlixxPlaceholder.literal("a", "X"), BlixxPlaceholder.literal("b", "Y")));

            assertEquals("X and Y", getClickActionValue(replaced));
        }
    }
}
