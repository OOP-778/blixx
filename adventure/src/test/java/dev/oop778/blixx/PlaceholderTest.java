package dev.oop778.blixx;

import dev.oop778.blixx.adventure.component.AdventureBlixxComponent;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.junit.jupiter.api.Test;

public class PlaceholderTest extends BaseBlixxTest {
    @Test
    void simplePlaceholderComparison() {
        final String input = "<red>Hello <player>!";

        final BlixxPlaceholder<String> blixxPlaceholder = BlixxPlaceholder.literal("player", "Player1");
        final TagResolver miniMessagePlaceholder = Placeholder.unparsed("player", "Player1");

        this.testPlaceholders(input, blixxPlaceholder, miniMessagePlaceholder);
    }

    @Test
    void decoratedPlaceholderComparison() {
        final String input = "<red>Hello <player_display_name>";
        final BlixxPlaceholder<BlixxComponent> blixxPlaceholder =
                BlixxPlaceholder.literal("player_display_name", BLIXX.parseComponent("<blue>Player1"));
        final TagResolver miniMessagePlaceholder =
                Placeholder.component("player_display_name", Component.text("Player1", NamedTextColor.BLUE));

        this.testPlaceholders(input, blixxPlaceholder, miniMessagePlaceholder);
    }

    protected void testPlaceholders(
            String input, BlixxPlaceholder<?> blixxPlaceholder, TagResolver miniMessagePlaceholder) {
        final Component miniMessage = MiniMessage.miniMessage().deserialize(input, miniMessagePlaceholder);
        final Component blixx = AdventureBlixxComponent.asComponent(
                BLIXX.parseComponent(input).replace(List.of(blixxPlaceholder), null));

        this.compareComponents(miniMessage, blixx);
    }
}
