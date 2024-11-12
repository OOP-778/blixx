package dev.oop778.blixx.benchmark;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTags;
import lombok.SneakyThrows;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

public class ReplaceBenchmark {
    private static final String INPUT = """
                <gradient:red:yellow>This is a <bold><italic>deeply nested</italic> string with
                <gradient:green:blue><bold><italic>nested gradients</italic></bold> and
                <hover:show_text:"<placeholder_1> Click me!"><click:run_command:"/say <placeholder_2>">interactive text</click></hover>.
                This message includes various <bold>types</bold> of <strikethrough>text decorations</strikethrough>,
                <obfuscated>obfuscated</obfuscated> text, <italic>emphasis</italic>, and a <gradient:#ff5555:#5555ff>gradient</gradient>.
                <italic>More placeholders:</italic> <placeholder_1> and <placeholder_2>.
                
                <gradient:#ff0000:#00ff00>Another complex line</gradient> with:
                <hover:show_text:"Tooltip for <placeholder_3>"><click:open_url:"https://example.com?ref=<placeholder_4>">Visit website</click></hover>,
                <click:suggest_command:"/help <placeholder_5>">suggested command</click>, and
                <hover:show_text:"Hover over <placeholder_6>"><click:run_command:"/list <placeholder_7>">list players</click></hover>.
                
                <gradient:yellow:light_purple>This <hover:show_text:"Placeholder <placeholder_8>">message</hover> tests placeholder density.</gradient>
                <placeholder_3>, <placeholder_4>, and more: <placeholder_5> all over this <bold>message</bold>.
                
                Let's add multiple types of placeholders:
                <gradient:#ff0000:#00ff00>Complex <hover:show_text:"Another <placeholder_9>">text</hover> with</gradient>
                <placeholder_6> and <placeholder_7> inside sentences.
                
                <gradient:#00ffff:#ff00ff>Final section</gradient> of <italic>complex</italic> text with:
                - <placeholder_8>
                - <placeholder_9>
                - <placeholder_10>
                <italic>End of the benchmark text with a final placeholder <placeholder_10>.</italic>
                """;
    private static final long WARMUP_TIME_SECONDS = 10;
    private static final long RUN_TIME_SECONDS = 10;

    @SneakyThrows
    public static void main(String[] args) {
        final Blixx blixx = Blixx.builder()
                .withStandardParserConfig((configurator) -> configurator
                        .withTags(BlixxTags.DEFAULT_TAGS)
                        .withPlaceholderFormat('<', '>')
                        .useKeyBasedPlaceholderIndexing())
                .withStandardPlaceholderConfig()
                .build();

        final BlixxComponent preparsed = blixx.parseComponent(INPUT);
        final List<BlixxPlaceholder<String>> placeholders = List.of(
                BlixxPlaceholder.literal("placeholder_1", "First placeholder"),
                BlixxPlaceholder.literal("placeholder_2", "Second placeholder"),
                BlixxPlaceholder.literal("placeholder_3", "Third placeholder"),
                BlixxPlaceholder.literal("placeholder_4", "Fourth placeholder"),
                BlixxPlaceholder.literal("placeholder_5", "Fifth placeholder"),
                BlixxPlaceholder.literal("placeholder_6", "Sixth placeholder"),
                BlixxPlaceholder.literal("placeholder_7", "Seventh placeholder"),
                BlixxPlaceholder.literal("placeholder_8", "Eighth placeholder"),
                BlixxPlaceholder.literal("placeholder_9", "Ninth placeholder"),
                BlixxPlaceholder.literal("placeholder_10", "Tenth placeholder")
        );
        final PlaceholderContext placeholderContext = PlaceholderContext.create();

        System.out.println(PlainTextComponentSerializer.plainText().serialize(preparsed.replace(placeholders, placeholderContext).asComponent()));

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < WARMUP_TIME_SECONDS * 1000) {
            UnaryOperator.identity().apply(preparsed.replace(placeholders, placeholderContext));
        }

        System.out.println("warmup done");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        reader.readLine();

        System.out.println("starting benchmark");
        startTime = System.currentTimeMillis();
        int times = 0;
        while (System.currentTimeMillis() - startTime < RUN_TIME_SECONDS * 1000) {
            UnaryOperator.identity().apply(preparsed.replace(placeholders, placeholderContext));
            times++;
        }

        System.out.printf("completed %s times%n", times);
        System.out.printf(
                "%.4f ns/op%n", (RUN_TIME_SECONDS / (double) times) * TimeUnit.SECONDS.toNanos(1)
        );
    }
}
