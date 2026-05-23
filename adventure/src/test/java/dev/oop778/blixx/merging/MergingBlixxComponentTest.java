package dev.oop778.blixx.merging;

import dev.oop778.blixx.BaseBlixxTest;
import dev.oop778.blixx.adventure.component.AdventureBlixxComponent;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

public class MergingBlixxComponentTest extends BaseBlixxTest {
    @Test
    void testBlixxMerge() {
        final BlixxComponent blixxComponent = BLIXX.parseComponent("<red>Hello");
        final BlixxComponent componentTwo = BLIXX.parseComponent(" <green>World");

        this.componentSerializesTo("&cHello&r &aWorld", blixxComponent.append(componentTwo));
    }

    @Test
    void testBlixxWithAdventureMerge() {
        final BlixxComponent one = AdventureBlixxComponent.wrap(Component.text("Hello, My name is Jeff "));
        final BlixxComponent two = BLIXX.parseComponent("<red>Hello");
        final BlixxComponent three = BLIXX.parseComponent(" <green>World");

        final BlixxComponent merged = one.append(two, three);

        this.componentSerializesTo("Hello, My name is Jeff &cHello&r &aWorld", merged);
    }

    @Test
    void testBlixxWithAdventureMergeTwoAndReplace() {
        final BlixxComponent one = BLIXX.parseComponent("Hello ");
        final TextComponent two = Component.text("World <hello>", NamedTextColor.RED);

        final BlixxPlaceholder<Integer> hello = BlixxPlaceholder.literal("hello", 1);
        this.componentSerializesTo(
                "Hello &cWorld <hello>",
                one.append(AdventureBlixxComponent.wrap(two)).replace(List.of(hello)));
    }
}
