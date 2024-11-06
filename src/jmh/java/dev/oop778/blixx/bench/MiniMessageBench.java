package dev.oop778.blixx.bench;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTags;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 1, time = 2)
@Fork(value = 1)
@Measurement(iterations = 1, time = 10)
public class MiniMessageBench {
    private Blixx blixxParser;
    private MiniMessage miniMessageParser;
    private String complexInput;
    private BlixxComponent preparsedBlixx;

    @Setup
    public void setup() {
        // Initialize Blixx parser
        this.blixxParser = Blixx.builder()
                .withStandardParserConfig((configurator) -> configurator
                        .withTags(BlixxTags.DEFAULT_TAGS)
                        .withPlaceholderFormat('<', '>')
                )
                .withStandardPlaceholderConfig()
                .build();

        // Initialize MiniMessage parser
        this.miniMessageParser = MiniMessage.miniMessage();

        // Complex input with various formatting, gradients, placeholders, etc.
        this.complexInput = """
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

        this.preparsedBlixx = this.blixxParser.parse(this.complexInput, PlaceholderContext.create());
    }

    @Benchmark
    public void blixxNoConversionNoPlaceholders() {
        this.blixxParser.parse(this.complexInput, PlaceholderContext.create());
    }

    @Benchmark
    public void blixxWithConversionNoPlaceholders() {
        final BlixxComponent parse = this.blixxParser.parse(this.complexInput, PlaceholderContext.create());
        parse.asComponent();
    }

    @Benchmark
    public void blixxWithConversionWithPlaceholders() {
        final BlixxComponent parse = this.blixxParser.parse(this.complexInput, PlaceholderContext.create());
        final BlixxPlaceholder<String> placeholder = BlixxPlaceholder.literal("placeholder_1", "1st placeholder");
        final BlixxPlaceholder<String> placeholder2 = BlixxPlaceholder.literal("placeholder_2", "2st placeholder");

        parse.replace(List.of(placeholder, placeholder2), PlaceholderContext.create()).asComponent();
    }

    @Benchmark
    public void blixxPreparsedWithPlaceholders() {
        final BlixxPlaceholder<String> placeholder = BlixxPlaceholder.literal("placeholder_1", "1st placeholder");
        final BlixxPlaceholder<String> placeholder2 = BlixxPlaceholder.literal("placeholder_2", "2st placeholder");

        this.preparsedBlixx.replace(List.of(placeholder, placeholder2), PlaceholderContext.create()).asComponent();
    }

    @Benchmark
    public void miniMessageNoPlaceholders() {
        this.miniMessageParser.deserialize(this.complexInput);
    }

    @Benchmark
    public void miniMessageWithPlaceholders() {
        this.miniMessageParser.deserialize(this.complexInput, Placeholder.parsed("placeholder_1", "1st placeholder"), Placeholder.parsed("placeholder_2", "2st placeholder"));
    }
}
