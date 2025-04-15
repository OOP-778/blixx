package dev.oop778.blixx.issue.cooldown;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.ComponentDecoration;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.processor.ReplacerProcessor;
import dev.oop778.blixx.api.tag.BlixxTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class CooldownIssue {
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
        final List<String> input = List.of(
                "",
                "{color}<bold>Kit Contents",
                "{contents}",
                "",
                "{color}Information",
                "{color}❙ <white>Cooldown: {color}{cooldown}",
                "",
                "<gray>You can claim this kit in {color}{cooldown_remaining}<gray>.");

        final BlixxComponent component = INSTANCE.parseComponent(String.join("\n", input));
        for (final Component component1 : ReplacerProcessor.adventureNewLineFlattener().accept(Replacer.createImmutable()
                .withLiteral("color", ComponentDecoration.of("<red>"))
                .withLiteral("contents", createContents(List.of(
                        Blixx.standard().parseComponent("1x Diamond Chestplate")
                )))
                .withLiteral("cooldown", "1")
                .withLiteral("cooldown_remaining", "2")
                .accept(component)
                .complete()
                .asComponent(Style.style()
                        .colorIfAbsent(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .build()))) {
            System.out.println(LegacyComponentSerializer.legacyAmpersand().serialize(component1));
        }
    }

    private static BlixxComponent createContents(List<BlixxComponent> rewards) {
        final List<BlixxComponent> components = new ArrayList<>();
        final Replacer replacer = Replacer.createImmutable().withLiteral("color", ComponentDecoration.of("<red>"));

        for (final BlixxComponent reward : rewards) {
            final BlixxComponent component = Blixx.standard().parseComponent("{color}| <white>").append(reward);
            components.add(replacer.accept(component).complete());
        }

        return components.stream().collect(BlixxComponent.joiningCollector(
                BlixxComponent.wrap(Component.empty()),
                false
        ));
    }
}
