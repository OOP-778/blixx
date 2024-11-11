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

@BenchmarkMode(value = {Mode.SingleShotTime, Mode.AverageTime, Mode.Throughput})
@State(Scope.Benchmark)
@Warmup(iterations = 1, time = 2)
@Fork(value = 1)
@Measurement(iterations = 1, time = 10)
@OutputTimeUnit(value = TimeUnit.MICROSECONDS)
public class MiniMessageBench {
    private Blixx blixxWithKeys;
    private Blixx blixxInMemoryIndex;
    private MiniMessage miniMessageParser;
    private String complexInput;
    private BlixxComponent preparsedWithKeys;
    private BlixxComponent preparsedInMemoryIndex;

    @Setup
    public void setup() {
        // Initialize Blixx parser
        this.blixxWithKeys = Blixx.builder()
                .withStandardParserConfig((configurator) -> configurator
                        .withTags(BlixxTags.DEFAULT_TAGS)
                        .withPlaceholderFormat('<', '>')
                        .useKeyBasedPlaceholderIndexing()
                )
                .withStandardPlaceholderConfig()
                .build();

        this.blixxInMemoryIndex = Blixx.builder()
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

        this.preparsedWithKeys = this.blixxWithKeys.parse(this.complexInput, PlaceholderContext.create());
        this.preparsedInMemoryIndex = this.blixxInMemoryIndex.parse(this.complexInput, PlaceholderContext.create());
    }

    @Benchmark
    public void blixxWithKeysParse() {
        this.blixxWithKeys.parse(this.complexInput, PlaceholderContext.create());
    }

    @Benchmark
    public void blixxWithKeysReplaceAndToComponent() {
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

        this.preparsedWithKeys.replace(placeholders, PlaceholderContext.create()).asComponent();
    }

    @Benchmark
    public void blixxWithMemoryIndexParse() {
        this.blixxInMemoryIndex.parse(this.complexInput, PlaceholderContext.create());
    }

    @Benchmark
    public void blixxWithMemoryIndexReplaceAndToComponent() {
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

        this.preparsedInMemoryIndex.replace(placeholders, PlaceholderContext.create()).asComponent();
    }

    @Benchmark
    public void miniMessageWithPlaceholders() {
        this.miniMessageParser.deserialize(
                this.complexInput,
                Placeholder.parsed("placeholder_1", "First placeholder"),
                Placeholder.parsed("placeholder_2", "Second placeholder"),
                Placeholder.parsed("placeholder_3", "Third placeholder"),
                Placeholder.parsed("placeholder_4", "Fourth placeholder"),
                Placeholder.parsed("placeholder_5", "Fifth placeholder"),
                Placeholder.parsed("placeholder_6", "Sixth placeholder"),
                Placeholder.parsed("placeholder_7", "Seventh placeholder"),
                Placeholder.parsed("placeholder_8", "Eighth placeholder"),
                Placeholder.parsed("placeholder_9", "Ninth placeholder"),
                Placeholder.parsed("placeholder_10", "Tenth placeholder")
        );
    }
}
