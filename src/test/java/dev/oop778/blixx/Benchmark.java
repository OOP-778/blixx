package dev.oop778.blixx;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.tag.BlixxTags;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

public class Benchmark {
    private static final String INPUT = """
            <gradient:red:yellow>This is a <bold><italic>deeply nested</italic> string with
            <gradient:green:blue><bold><italic>nested gradients</italic></bold> and
            <hover:show_text:"Click me!"><click:run_command:"/say Hello!">interactive text</click></hover>.
            We also have multiple <bold>types</bold> of <strikethrough>text decorations</strikethrough> and
            <obfuscated>obfuscated</obfuscated> text for testing purposes.
            <gradient:#ff5555:#5555ff>Another gradient</gradient> with <italic><bold>placeholders</bold>:</italic>
            <gradient:#00ff00:#0000ff>{primary_color} Primary, {secondary_color} Secondary</gradient>, and more.
            <hover:show_text:"Tooltip!"><click:open_url:"https://example.com">Visit website</click></hover>.
            Even more <gradient:#ff0000:#00ff00>complex</gradient> nodes with <gradient:#00ffff:#ff00ff>crazy gradients</gradient> 
            and <hover:show_text:"Hover info"><click:suggest_command:"/help">suggested commands</click></hover>.
            """;
    private static final long WARMUP_TIME_SECONDS = 10;
    private static final long RUN_TIME_SECONDS = 10;

    @SneakyThrows
    public static void main(String[] args) {
        final Blixx blixx = Blixx.builder()
                .withStandardParserConfig((configurator) -> configurator
                        .withTags(BlixxTags.DEFAULT_TAGS)
                        .withPlaceholderFormat('%', '%')
                        .withPlaceholderFormat('{', '}'))
                .withStandardPlaceholderConfig()
                .build();

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < WARMUP_TIME_SECONDS * 1000) {
            final BlixxComponent parse = blixx.parse(INPUT);
            UnaryOperator.identity().apply(parse.asComponent());
        }

        System.out.println("warmup done");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        reader.readLine();

        System.out.println("starting benchmark");
        startTime = System.currentTimeMillis();
        int times = 0;
        while (System.currentTimeMillis() - startTime < RUN_TIME_SECONDS * 1000) {
            UnaryOperator.identity().apply(blixx.parse(INPUT));
            times++;
        }

        System.out.println("completed %s times".formatted(times));
        System.out.println("%.4f ns/op".formatted(
                (RUN_TIME_SECONDS / (double) times) * TimeUnit.SECONDS.toNanos(1)
        ));
    }
}
