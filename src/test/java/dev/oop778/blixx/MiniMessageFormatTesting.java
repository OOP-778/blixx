package dev.oop778.blixx;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MiniMessageFormatTesting extends BaseBlixxTest {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Test
    void testNamedColor() {
        this.testInput("<yellow>Hello <blue>World");
    }

    @Test
    void testHexColor() {
        this.testInput("<#00ff00>R G B!");
    }

    @Test
    void testReset() {
        this.testInput("<yellow><bold>Hello <reset>world!");
    }

//    @Test
//    void testGradient() {
//        this.testInput("<yellow>Woo: <gradient:green:blue>HELLO IM GAY <green>yay");
//    }

    private void testInput(String input) {
        final Component blixxParse = BLIXX.parse(input).asComponent();
        final Component adventureParse = MINI_MESSAGE.deserialize(input);

        if (Objects.equals(blixxParse, adventureParse)) {
            return;
        }

        final String blixxString = LegacyComponentSerializer.legacyAmpersand().serialize(blixxParse);
        final String adventureString = LegacyComponentSerializer.legacyAmpersand().serialize(adventureParse);

        assertEquals(adventureString, blixxString);
    }
}
