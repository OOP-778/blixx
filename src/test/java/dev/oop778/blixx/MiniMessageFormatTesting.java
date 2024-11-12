package dev.oop778.blixx;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

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

    private void testInput(String input) {
        final Component blixxParse = BLIXX.parseComponent(input).asComponent();
        final Component adventureParse = MINI_MESSAGE.deserialize(input);
        this.compareComponents(adventureParse, blixxParse);
    }
}
