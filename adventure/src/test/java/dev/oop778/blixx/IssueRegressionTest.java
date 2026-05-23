package dev.oop778.blixx;

import static org.junit.jupiter.api.Assertions.*;

import dev.oop778.blixx.adventure.component.AdventureBlixxComponent;
import dev.oop778.blixx.adventure.replacer.AdventureReplacerProcessor;
import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.ComponentDecoration;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

class IssueRegressionTest extends BaseBlixxTest {
    @Test
    void colorResetDoesNotBleedIntoNextPlaceholder() {
        final BlixxComponent input = BLIXX.parseComponent(
                "<yellow><b>Boosters <dark_gray>:: <gray>Gave booster {color}{booster} <gray>with duration <white>{duration} <gray>to <white>{player}");
        final Component component = AdventureBlixxComponent.asComponent(Replacer.createImmutable()
                .withLiteral("color", ComponentDecoration.of("<aqua>"))
                .withLiteral("booster", "Tokens with duration")
                .withLiteral("duration", "1m")
                .withLiteral("player", "OOP_778")
                .accept(input)
                .complete());

        String serialized = LegacyComponentSerializer.legacyAmpersand().serialize(component);
        assertFalse(serialized.isEmpty());
        assertTrue(serialized.contains("Tokens with duration"));
        assertTrue(serialized.contains("1m"));
        assertTrue(serialized.contains("OOP_778"));
    }

    @Test
    void cooldownMessageWithDecorations() {
        final List<String> input = List.of(
                "",
                "{color}Kit Contents",
                "{contents}",
                "",
                "{color}Information",
                "{color}❙ <white>Cooldown: {color}{cooldown}",
                "",
                "<gray>Click to {color}claim <gray>this kit.");

        final BlixxComponent component = BLIXX.parseComponent(String.join("\n", input));
        final BlixxComponent complete = Replacer.createImmutable()
                .withLiteral("color", ComponentDecoration.of("<red>"))
                .withLiteral(
                        "contents", createContents(List.of(Blixx.standard().parseComponent("1x Diamond Chestplate"))))
                .withLiteral("cooldown", "1")
                .withLiteral("cooldown_remaining", "2")
                .accept(component)
                .complete();

        List<Component> lines = AdventureReplacerProcessor.newLineFlattener()
                .accept(AdventureBlixxComponent.asComponent(
                        complete,
                        Style.style()
                                .colorIfAbsent(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .build()));

        assertFalse(lines.isEmpty());
        String fullText = lines.stream()
                .map(c -> PlainTextComponentSerializer.plainText().serialize(c))
                .reduce("", (a, b) -> a + "\n" + b);
        assertTrue(fullText.contains("Kit Contents"));
        assertTrue(fullText.contains("Cooldown"));
    }

    @Test
    void issue2HoverWithPlaceholder() {
        final BlixxComponent parse = BlixxComponent.parse(
                "<hover:show_text:'{breakdown}'><yellow><b>Enchants</b> <dark_gray>:: {enchant} <gray>has activated! <gray>(Hover for info)");
        Component component = AdventureBlixxComponent.asComponent(
                parse.replace(Replacer.createImmutable(BlixxPlaceholder.literal("breakdown", "GANG NAME"))));

        assertNotNull(component);
        String serialized = PlainTextComponentSerializer.plainText().serialize(component);
        assertTrue(serialized.contains("Enchants"));
        assertTrue(serialized.contains("has activated"));
    }

    @Test
    void issue3RewardTemplateWithColorDecoration() {
        final BlixxComponent rewardTemplate = BlixxComponent.parse("{color}❙ {/color}<white>{reward}");
        final BlixxComponent displayName = Replacer.createImmutable()
                .withLiteral("color", ComponentDecoration.of("&7"))
                .withLiteral("name", "Coal")
                .accept(BlixxComponent.parse("{color}{name} Robot{/color}"))
                .complete();

        final ComponentDecoration decoration = ComponentDecoration.of("&c");
        BlixxComponent result = rewardTemplate.replace(
                Replacer.createImmutable().withLiteral("color", decoration).withLiteral("reward", displayName));

        String serialized =
                LegacyComponentSerializer.legacyAmpersand().serialize(AdventureBlixxComponent.asComponent(result));
        assertFalse(serialized.isEmpty());
    }

    @Test
    void issue4MultilineXpDisplay() {
        final BlixxComponent blixxComponent = Blixx.standard()
                .parseComponent(String.join(
                        "\n",
                        List.of(
                                "<gold>› <gray>XP Until Next Level: <gold>{xp}/{xp_required}",
                                " ",
                                "<gold>You're currently at this armor type!")));
        final BlixxComponent complete = Replacer.createImmutable(
                        BlixxPlaceholder.literal("xp", 10), BlixxPlaceholder.literal("xp_required", 100))
                .accept(blixxComponent)
                .complete();

        List<Component> lines = AdventureReplacerProcessor.newLineFlattener()
                .accept(AdventureBlixxComponent.asComponent(
                        complete,
                        Style.style()
                                .colorIfAbsent(NamedTextColor.WHITE)
                                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .build()));

        assertFalse(lines.isEmpty());
        String fullText = lines.stream()
                .map(c -> PlainTextComponentSerializer.plainText().serialize(c))
                .reduce("", (a, b) -> a + "\n" + b);
        assertTrue(fullText.contains("10"));
        assertTrue(fullText.contains("100"));
    }

    @Test
    void issue5ItemDisplayWithHoverEvent() {
        BlixxComponent itemDisplay = BlixxComponent.parse("<dark_gray>» {item} <dark_gray>«");

        BlixxComponent itemComponent = Replacer.createImmutable()
                .withLiteral(
                        "item",
                        () -> AdventureBlixxComponent.wrap(Component.text("GAY")
                                .hoverEvent(HoverEvent.showItem(HoverEvent.ShowItem.showItem(Key.key("gay"), 1)))))
                .accept(itemDisplay)
                .complete();

        BlixxComponent parse = BlixxComponent.parse("{item} {item}");
        BlixxComponent result = parse.replace(Replacer.createImmutable().withLiteral("item", () -> itemComponent));

        assertNotNull(result);
    }

    @Test
    void simplePlaceholderAndDecorationReplacement() {
        final BlixxComponent component = Blixx.standard().parseComponent("{color_1}{name} {level}{/color_1}");
        final Replacer player = Replacer.createImmutable()
                .withLiteral("color_1", ComponentDecoration.of("&e"))
                .withLiteral("name", "oop")
                .withLiteral("level", 1);

        String serialized = LegacyComponentSerializer.legacyAmpersand()
                .serialize(AdventureBlixxComponent.asComponent(component.replace(player)));
        assertFalse(serialized.isEmpty());
        assertTrue(serialized.contains("oop"));
    }

    @Test
    void unclosedTagsDoNotThrow() {
        final BlixxComponent blixxComponent = Blixx.standard()
                .parseComponent("<hover:show_text:\"&7Click to reply to {player}\">&8[&6PM&8] &eme &8- "
                        + "</click></hover> &8\\> &f{message}");
        final BlixxComponent one = blixxComponent.replace(List.of(BlixxPlaceholder.literal("player", "test1")));
        final BlixxComponent two = blixxComponent.replace(List.of(BlixxPlaceholder.literal("player", "test2")));

        String serialized =
                PlainTextComponentSerializer.plainText().serialize(AdventureBlixxComponent.asComponent(one));
        assertFalse(serialized.isEmpty());

        String serialized2 =
                PlainTextComponentSerializer.plainText().serialize(AdventureBlixxComponent.asComponent(two));
        assertFalse(serialized2.isEmpty());
    }

    @Test
    void gradientRendering() {
        final BlixxComponent component = BLIXX.parseComponent("<bold><gradient:#00FF00:#FF007F>Hello World");
        final Component adventureComponent = AdventureBlixxComponent.asComponent(component);
        assertNotNull(adventureComponent);
        String serialized = LegacyComponentSerializer.legacyAmpersand().serialize(adventureComponent);
        assertFalse(serialized.isEmpty());
    }

    @Test
    void placeholderReplacementWithColors() {
        final String input = "<red>Hello {name} <blue>welcome!";

        final BlixxComponent parsed = BLIXX.parseComponent(input);
        final BlixxComponent replaced = parsed.replace(List.of(BlixxPlaceholder.literal("name", "Steve")), null);

        String serialized =
                LegacyComponentSerializer.legacyAmpersand().serialize(AdventureBlixxComponent.asComponent(replaced));
        assertFalse(serialized.isEmpty());
        assertTrue(serialized.contains("Steve"));
        assertTrue(serialized.contains("welcome"));
    }

    private static BlixxComponent createContents(List<BlixxComponent> rewards) {
        final List<BlixxComponent> components = new ArrayList<>();
        final Replacer replacer = Replacer.createImmutable().withLiteral("color", ComponentDecoration.of("<red>"));

        for (final BlixxComponent reward : rewards) {
            final BlixxComponent component =
                    BLIXX.parseComponent("{color}| <white>").append(reward);
            components.add(replacer.accept(component).complete());
        }

        return components.stream()
                .collect(BlixxComponent.joiningCollector(AdventureBlixxComponent.wrap(Component.empty()), false));
    }
}
