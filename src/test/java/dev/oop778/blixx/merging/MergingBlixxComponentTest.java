package dev.oop778.blixx.merging;

import dev.oop778.blixx.BaseBlixxTest;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MergingBlixxComponentTest extends BaseBlixxTest {

    @Test
    void testBlixxMerge() {
        final BlixxComponent blixxComponent = BLIXX.parseComponent("<red>Hello");
        final BlixxComponent componentTwo = BLIXX.parseComponent(" <green>World");

        this.componentSerializesTo("&cHello&r &aWorld", blixxComponent.append(componentTwo));
    }

    @Test
    void testBlixxWithAdventureMerge() {
        final BlixxComponent one = BlixxComponent.wrap(Component.text("Hello, My name is Jeff "));
        final BlixxComponent two = BLIXX.parseComponent("<red>Hello");
        final BlixxComponent three = BLIXX.parseComponent(" <green>World");

        final BlixxComponent merged = one.append(two, three);

        System.out.println(LegacyComponentSerializer.legacyAmpersand().serialize(merged.asComponent()));
    }

    @Test
    void testBlixxWithAdventureMergeTwoAndReplace() {
        final BlixxComponent one = BLIXX.parseComponent("Hello ");
        final TextComponent two = Component.text("World <hello>", NamedTextColor.RED);

        final BlixxPlaceholder<Integer> hello = BlixxPlaceholder.literal("hello", 1);
        this.componentSerializesTo("Hello &cWorld <hello>", one.append(two).replace(List.of(hello)));
    }
}
