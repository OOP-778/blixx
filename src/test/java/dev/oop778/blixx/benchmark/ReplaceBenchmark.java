package dev.oop778.blixx.benchmark;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTags;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

public class ReplaceBenchmark {
    private static final String INPUT = """
                <gradient:red:yellow>This is a <bold><italic>deeply nested</italic> string with
                <gradient:green:blue><bold><italic>nested gradients</italic></bold> and
                <hover:show_text:"Click me!"><click:run_command:"/say Hello!">interactive text</click></hover>.
                We also have multiple <bold>types</bold> of <strikethrough>text decorations</strikethrough> and
                <obfuscated>obfuscated</obfuscated> text for testing purposes.
                <gradient:#ff5555:#5555ff>Another gradient</gradient> with <italic><bold>placeholders</bold>:</italic>
                <gradient:#00ff00:#0000ff><placeholder_1> Placeholder</gradient>, and more.
                <hover:show_text:"Tooltip!"><click:open_url:"https://example.com">Visit website</click></hover>.
                Even more <gradient:#ff0000:#00ff00>complex</gradient> nodes with <gradient:#00ffff:#ff00ff>crazy gradients</gradient> 
                and <hover:show_text:"Hover info"><click:suggest_command:"/help">suggested commands</click></hover>.
                <placeholder_2>
                """;
    private static final long WARMUP_TIME_SECONDS = 10;
    private static final long RUN_TIME_SECONDS = 10;

    @SneakyThrows
    public static void main(String[] args) {
        final Blixx blixx = Blixx.builder()
                .withStandardParserConfig((configurator) -> configurator
                        .withTags(BlixxTags.DEFAULT_TAGS)
                        .withPlaceholderFormat('<', '>'))
                .withStandardPlaceholderConfig()
                .build();

        final BlixxComponent preparsed = blixx.parse(INPUT);
        final BlixxPlaceholder<String> placeholder = BlixxPlaceholder.literal("placeholder_1", "1st placeholder");
        final BlixxPlaceholder<String> placeholder2 = BlixxPlaceholder.literal("placeholder_2", "2st placeholder");
        final List<BlixxPlaceholder<String>> placeholders = List.of(placeholder, placeholder2);
        final PlaceholderContext placeholderContext = PlaceholderContext.create();

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
