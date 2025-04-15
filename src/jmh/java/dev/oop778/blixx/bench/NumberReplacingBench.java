package dev.oop778.blixx.bench;

import dev.oop778.blixx.NumberUtility;
import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@BenchmarkMode(value = {Mode.SingleShotTime, Mode.AverageTime, Mode.Throughput})
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 2)
@Fork(value = 1)
@Measurement(iterations = 1, time = 10)
@Timeout(time = 1)
@OutputTimeUnit(value = TimeUnit.MICROSECONDS)
public class NumberReplacingBench {
    private BlixxComponent input;

    public static Replacer createReplacerComposed(String name, Number number) {
        return Replacer.createImmutable(
                BlixxPlaceholder.literal(name, number),
                BlixxPlaceholder.literal(name + "_commas", NumberUtility.Formatter.formatWithCommas(number)),
                BlixxPlaceholder.literal(name + "_formatted_sl", NumberUtility.Formatter.format(number, true, true)),
                BlixxPlaceholder.literal(name + "_formatted_ll", NumberUtility.Formatter.format(number, false, true)),
                BlixxPlaceholder.literal(name + "_formatted_ls", NumberUtility.Formatter.format(number, false, false)),
                BlixxPlaceholder.literal(name + "_formatted_ss", NumberUtility.Formatter.format(number, true, false)));
    }

    public static void main(String[] args) {
        final List<String> inputs = List.of(
                "Hello <number>",
                "<number_commas>",
                "<number_sl>",
                "<number_ll>",
                "<number_ls>",
                "<number_ss>"
        );
        final long number = 13245678;
        final BlixxPlaceholder<String> placeholder = numberReplacingPattern("number", number);

        for (final String input : inputs) {
            System.out.println(input);
            System.out.println(PlainTextComponentSerializer.plainText().serialize(Blixx.standard().parseComponent(input).replace(List.of(placeholder)).asComponent()));
        }
    }

    public static BlixxPlaceholder<String> numberReplacingPattern(String name, Number number) {
        return BlixxPlaceholder.<String>builder()
                .pattern()
                .withPattern(Pattern.compile("%s([a-zA-Z_]+)?".formatted(Pattern.quote(name))))
                .withMatcherSupplying((matcher) -> {
                    if (matcher.group(1) == null) {
                        return number.toString();
                    }

                    final String remaining = matcher.group(1);
                    return switch (remaining) {
                        case "_commas" -> NumberUtility.Formatter.formatWithCommas(number);
                        case "_sl" -> NumberUtility.Formatter.format(number, true, true);
                        case "_ll" -> NumberUtility.Formatter.format(number, false, true);
                        case "_ls" -> NumberUtility.Formatter.format(number, true, false);
                        case "_ss" -> NumberUtility.Formatter.format(number, false, false);
                        default -> number.toString();
                    };
                })
                .build();
    }

    @Setup
    public void setup() {
        final String complexInputSingleNumber = """
                <gradient:green:yellow>Demonstrating formatting with a <bold>single number</bold>: <number_1>.</gradient>
                Basic number: <number_1>
                With commas: <number_1_commas>
                Formatted (short, long): <number_1_formatted_sl>
                Formatted (long, long):  <number_1_formatted_ll>
                Formatted (long, short): <number_1_formatted_ls>
                Formatted (short, short): <number_1_formatted_ss>
                
                <gradient:blue:yellow>Interactive examples using only <number_1>:</gradient>
                <hover:show_text:"<number_1_commas> (Hover for details)"><click:run_command:"/say Value is <number_1_formatted_sl>">Click to show formatted value</click></hover>.
                <click:open_url:"https://example.com?n=<number_1>">Visit website with number</click>
                <click:suggest_command:"/give @s diamond <number_1_commas>">Suggest giving diamonds</click>
                <hover:show_text:"Copy <number_1_formatted_ll> to clipboard"><click:copy_to_clipboard:"<number_1>">Copy raw number</click></hover>
                
                <gradient:#ff5555:#5555ff>Nested formatting and placeholders:</gradient>
                <bold><italic>Number inside bold and italic: <number_1_formatted_ss></italic></bold>
                <underline>Underlined number: <number_1_commas></underline>
                <strikethrough>Strikethrough: <number_1_formatted_ls></strikethrough>
                <obfuscated>Obfuscated: <number_1></obfuscated> (should still replace!)
                <gradient:red:green>Gradient with <hover:show_text:"Raw: <number_1>"><number_1_formatted_sl></hover></gradient>
                
                <gradient:aqua:green>Multiple placeholders in one element:</gradient>
                <hover:show_text:"<number_1>, <number_1_commas>, <number_1_formatted_sl>, <number_1_formatted_ll>"><click:run_command:"/tellraw @a {"text":"<number_1_formatted_ls> / <number_1_formatted_ss>"}">All formats in one hover/click</click></hover>
                
                Final section, demonstrating a list:
                - Raw: <number_1>
                - Commas: <number_1_commas>
                - Short, Long: <number_1_formatted_sl>
                <italic>The End.  All examples used variations of the <bold>same single number</bold> placeholder: <number_1></italic>.
                """;

        this.input = Blixx.standard().parseComponent(complexInputSingleNumber);
    }

    @Benchmark
    public void placeholderPattern() {
        try {
            final BlixxPlaceholder<String> placeholder = numberReplacingPattern("number_1", 25125151515L);
            final BlixxComponent replace = this.input.replace(List.of(placeholder));

            final Component component = replace.asComponent();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Benchmark
    public void replacer() {
        try {
            final Replacer replacer = createReplacerComposed("number_1", 25125151515L);
            final BlixxComponent replace = replacer.accept(this.input).complete();

            final Component component = replace.asComponent();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
