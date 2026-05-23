package dev.oop778.blixx.api;

import dev.oop778.blixx.api.formatter.BlixxFormatters;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.platform.BlixxPlatform;
import dev.oop778.blixx.api.tag.BlixxTag;
import java.util.Map;
import java.util.function.Consumer;
import lombok.NonNull;

public interface BlixxBuilder {
    interface ParserSelectorPart<T> {
        T withStandardParserConfig();

        T withStandardParserConfig(@NonNull Consumer<ParserConfigurator<?>> consumer);

        T withCustomParserConfig(@NonNull Consumer<ParserConfigurator<?>> consumer);
    }

    interface PlaceholderSelectorPart<T> {
        T withStandardPlaceholderConfig();

        T withStandardPlaceholderConfig(@NonNull Consumer<PlaceholderConfigurator<?>> consumer);

        T withCustomPlaceholderConfig(@NonNull Consumer<PlaceholderConfigurator<?>> consumer);
    }

    interface OptionalStep {
        OptionalStep withPlatform(@NonNull BlixxPlatform platform);

        Blixx build();
    }

    interface ParserConfigurator<T> {
        ParserConfigurator<T> withStandardTags();

        ParserConfigurator<T> withStandardPlaceholderFormat();

        ParserConfigurator<T> withPlaceholderFormat(char start, char end);

        ParserConfigurator<T> withTag(@NonNull BlixxTag<?> tag, @NonNull String... keys);

        ParserConfigurator<T> withTags(@NonNull Map<String, BlixxTag<?>> tags);

        ParserConfigurator<T> withParsePlaceholder(BlixxPlaceholder<String> placeholder);
    }

    interface PlaceholderConfigurator<T> {
        PlaceholderConfigurator<T> withFormatter(@NonNull Consumer<PlaceholderFormatterConfigurator<?>> consumer);

        PlaceholderConfigurator<T> withDefaultContext(
                @NonNull Consumer<PlaceholderDefaultContextConfigurator<?>> consumer);
    }

    interface PlaceholderFormatSupportConfigurator<T> {
        PlaceholderFormatSupportConfigurator<T> withStandard();

        PlaceholderFormatSupportConfigurator<T> withFormat(char start, char end);
    }

    interface PlaceholderFormatterConfigurator<T> {
        PlaceholderFormatterConfigurator<T> withStandard();

        PlaceholderFormatterConfigurator<T> withFormatters(BlixxFormatters formatters);
    }

    interface PlaceholderDefaultContextConfigurator<T> {
        <C> PlaceholderDefaultContextConfigurator<T> withDefaultInheritanceContext(@NonNull C instance);

        <C> PlaceholderDefaultContextConfigurator<T> withDefaultContext(@NonNull C instance);
    }
}
