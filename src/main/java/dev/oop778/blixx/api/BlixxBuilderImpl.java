package dev.oop778.blixx.api;

import dev.oop778.blixx.api.formatter.BlixxDefaultFormatters;
import dev.oop778.blixx.api.formatter.BlixxFormatters;
import dev.oop778.blixx.api.parser.config.ParserConfigImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.PlaceholderConfigImpl;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContextImpl;
import dev.oop778.blixx.api.platform.BlixxPlatform;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.api.tag.BlixxTags;
import dev.oop778.blixx.util.Pair;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class BlixxBuilderImpl
        implements BlixxBuilder.OptionalStep,
                BlixxBuilder.PlaceholderSelectorPart<BlixxBuilderImpl>,
                BlixxBuilder.ParserSelectorPart<BlixxBuilderImpl> {
    private Function<ParserConfiguratorImpl<BlixxBuilderImpl>, ParserConfiguratorImpl<BlixxBuilderImpl>>
            parserConfigurator = UnaryOperator.identity();
    private Function<PlaceholderConfiguratorImpl<BlixxBuilderImpl>, PlaceholderConfiguratorImpl<BlixxBuilderImpl>>
            placeholderConfigurator = UnaryOperator.identity();
    private BlixxPlatform platform;

    @Override
    public BlixxBuilder.OptionalStep withPlatform(@NonNull BlixxPlatform platform) {
        this.platform = platform;
        return this;
    }

    @Override
    public Blixx build() {
        final ParserConfigImpl parserConfig =
                this.parserConfigurator.apply(new ParserConfiguratorImpl<>()).build();
        final PlaceholderConfigImpl placeholderConfig = this.placeholderConfigurator
                .apply(new PlaceholderConfiguratorImpl<>())
                .build();
        return new BlixxImpl(parserConfig, placeholderConfig, this.platform);
    }

    @Override
    public BlixxBuilderImpl withStandardParserConfig() {
        this.parserConfigurator = configurator -> {
            configurator.withStandardPlaceholderFormat().withStandardTags();
            return configurator;
        };
        return this;
    }

    @Override
    public BlixxBuilderImpl withStandardParserConfig(@NonNull Consumer<BlixxBuilder.ParserConfigurator<?>> consumer) {
        this.withStandardParserConfig();
        this.parserConfigurator = this.parserConfigurator.andThen((configurator) -> {
            consumer.accept(configurator);
            return configurator;
        });
        return this;
    }

    @Override
    public BlixxBuilderImpl withCustomParserConfig(@NonNull Consumer<BlixxBuilder.ParserConfigurator<?>> consumer) {
        this.parserConfigurator = this.parserConfigurator.andThen(configurator -> {
            consumer.accept(configurator);
            return configurator;
        });
        return this;
    }

    @Override
    public BlixxBuilderImpl withStandardPlaceholderConfig() {
        this.placeholderConfigurator = (configurator) -> {
            configurator.withFormatter(BlixxBuilder.PlaceholderFormatterConfigurator::withStandard);
            return configurator;
        };
        return this;
    }

    @Override
    public BlixxBuilderImpl withStandardPlaceholderConfig(
            @NonNull Consumer<BlixxBuilder.PlaceholderConfigurator<?>> consumer) {
        this.withStandardPlaceholderConfig();
        this.placeholderConfigurator = this.placeholderConfigurator.andThen(configurator -> {
            consumer.accept(configurator);
            return configurator;
        });
        return this;
    }

    @Override
    public BlixxBuilderImpl withCustomPlaceholderConfig(
            @NonNull Consumer<BlixxBuilder.PlaceholderConfigurator<?>> consumer) {
        this.placeholderConfigurator = this.placeholderConfigurator.andThen(configurator -> {
            consumer.accept(configurator);
            return configurator;
        });
        return this;
    }

    @ApiStatus.Internal
    public static class ParserConfiguratorImpl<T> implements BlixxBuilder.ParserConfigurator<T> {
        private final Map<String, BlixxTag<?>> tags = new HashMap<>();
        private final Set<Pair<Character, Character>> formats = new HashSet<>();
        private final List<BlixxPlaceholder<String>> parsePlaceholders = new ArrayList<>();
        private final char tagOpen = '<';
        private final char tagClose = '>';

        @Override
        public BlixxBuilder.ParserConfigurator<T> withStandardTags() {
            return this.withTags(BlixxTags.STANDARD);
        }

        @Override
        public BlixxBuilder.ParserConfigurator<T> withStandardPlaceholderFormat() {
            return this.withPlaceholderFormat('<', '>');
        }

        @Override
        public BlixxBuilder.ParserConfigurator<T> withPlaceholderFormat(char start, char end) {
            this.formats.add(new Pair<>(start, end));
            return this;
        }

        @Override
        public BlixxBuilder.ParserConfigurator<T> withTag(@NonNull BlixxTag<?> tag, @NotNull @NonNull String... keys) {
            for (@NotNull @NonNull final String key : keys) {
                this.tags.put(key, tag);
            }
            return this;
        }

        @Override
        public BlixxBuilder.ParserConfigurator<T> withTags(@NonNull Map<String, BlixxTag<?>> tags) {
            this.tags.putAll(tags);
            return this;
        }

        @Override
        public BlixxBuilder.ParserConfigurator<T> withParsePlaceholder(BlixxPlaceholder<String> placeholder) {
            this.parsePlaceholders.add(placeholder);
            return this;
        }

        public ParserConfigImpl build() {
            return new ParserConfigImpl(
                    this.tags,
                    new ArrayList<>(this.formats),
                    this.parsePlaceholders,
                    this.tagOpen,
                    this.tagClose,
                    true);
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    public static class PlaceholderConfiguratorImpl<T> implements BlixxBuilder.PlaceholderConfigurator<T> {
        private final Function<PlaceholderFormatterConfiguratorImpl<?>, PlaceholderFormatterConfiguratorImpl<?>>
                formatter = UnaryOperator.identity();
        private final Function<
                        PlaceholderDefaultContextConfiguratorImpl<?>, PlaceholderDefaultContextConfiguratorImpl<?>>
                defaultContext = UnaryOperator.identity();

        @Override
        public BlixxBuilder.PlaceholderConfigurator<T> withFormatter(
                @NonNull Consumer<BlixxBuilder.PlaceholderFormatterConfigurator<?>> consumer) {
            this.formatter.andThen(formatter -> {
                consumer.accept(formatter);
                return formatter;
            });
            return this;
        }

        @Override
        public BlixxBuilder.PlaceholderConfigurator<T> withDefaultContext(
                @NonNull Consumer<BlixxBuilder.PlaceholderDefaultContextConfigurator<?>> consumer) {
            this.defaultContext.andThen(defaultContext -> {
                consumer.accept(defaultContext);
                return defaultContext;
            });
            return this;
        }

        public PlaceholderConfigImpl build() {
            final PlaceholderFormatterConfiguratorImpl<?> formatterConfigurator =
                    this.formatter.apply(new PlaceholderFormatterConfiguratorImpl<>());
            final PlaceholderDefaultContextConfiguratorImpl<?> contextConfigurator =
                    this.defaultContext.apply(new PlaceholderDefaultContextConfiguratorImpl<>());
            return new PlaceholderConfigImpl(
                    formatterConfigurator.defaultFormatters, contextConfigurator.defaultContext);
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    public static class PlaceholderFormatterConfiguratorImpl<T>
            implements BlixxBuilder.PlaceholderFormatterConfigurator<T> {
        BlixxFormatters defaultFormatters;

        @Override
        public BlixxBuilder.PlaceholderFormatterConfigurator<T> withStandard() {
            this.defaultFormatters = BlixxDefaultFormatters.getDefault();
            return this;
        }

        @Override
        public BlixxBuilder.PlaceholderFormatterConfigurator<T> withFormatters(BlixxFormatters formatters) {
            this.defaultFormatters = formatters;
            return this;
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    public static class PlaceholderDefaultContextConfiguratorImpl<T>
            implements BlixxBuilder.PlaceholderDefaultContextConfigurator<T> {
        final PlaceholderContextImpl defaultContext = new PlaceholderContextImpl();

        @Override
        public <C> BlixxBuilder.PlaceholderDefaultContextConfigurator<T> withDefaultInheritanceContext(
                @NonNull C instance) {
            this.defaultContext.registerForHierarchy(instance);
            return this;
        }

        @Override
        public <C> BlixxBuilder.PlaceholderDefaultContextConfigurator<T> withDefaultContext(@NonNull C instance) {
            this.defaultContext.register(instance);
            return this;
        }
    }
}
