package dev.oop778.blixx.api;

import dev.oop778.blixx.api.formatter.BlixxDefaultFormatters;
import dev.oop778.blixx.api.formatter.BlixxFormatters;
import dev.oop778.blixx.api.parser.config.ParserConfigImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.PlaceholderConfigImpl;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContextImpl;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.api.tag.BlixxTags;
import dev.oop778.blixx.util.Pair;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

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
        Blixx build();
    }

    interface ParserConfigurator<T> {
        ParserConfigurator<T> withStandardTags();

        ParserConfigurator<T> withStandardPlaceholderFormat();

        ParserConfigurator<T> withPlaceholderFormat(char start, char end);

        ParserConfigurator<T> withTag(@NonNull BlixxTag<?> tag, @NonNull String... keys);

        ParserConfigurator<T> withTags(@NonNull Map<String, BlixxTag<?>> tags);

        ParserConfigurator<T> withParsePlaceholder(BlixxPlaceholder<String> placeholder);

        ParserConfigurator<T> useKeyBasedPlaceholderIndexing();
    }

    interface PlaceholderConfigurator<T> {
        PlaceholderConfigurator<T> withFormatter(@NonNull Consumer<PlaceholderFormatterConfigurator<?>> consumer);

        PlaceholderConfigurator<T> withDefaultContext(@NonNull Consumer<PlaceholderDefaultContextConfigurator<?>> consumer);
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

    @ApiStatus.Internal
    class ParserConfiguratorImpl<T> implements ParserConfigurator<T> {
        private final Map<String, BlixxTag<?>> tags = new HashMap<>();
        private final Set<Pair<Character, Character>> formats = new HashSet<>();
        private final List<BlixxPlaceholder<String>> parsePlaceholders = new ArrayList<>();
        private final char tagOpen = '<';
        private final char tagClose = '>';
        private boolean useKeyBasedIndexing;

        @Override
        public ParserConfigurator<T> withStandardTags() {
            return this.withTags(BlixxTags.STANDARD);
        }

        @Override
        public ParserConfigurator<T> withStandardPlaceholderFormat() {
            return this.withPlaceholderFormat('<', '>');
        }

        @Override
        public ParserConfigurator<T> withPlaceholderFormat(char start, char end) {
            this.formats.add(new Pair<>(start, end));
            return this;
        }

        @Override
        public ParserConfigurator<T> withTag(@NonNull BlixxTag<?> tag, @NotNull @NonNull String... keys) {
            for (@NotNull @NonNull final String key : keys) {
                this.tags.put(key, tag);
            }

            return this;
        }

        @Override
        public ParserConfigurator<T> withTags(@NonNull Map<String, BlixxTag<?>> tags) {
            this.tags.putAll(tags);
            return this;
        }

        @Override
        public ParserConfigurator<T> withParsePlaceholder(BlixxPlaceholder<String> placeholder) {
            this.parsePlaceholders.add(placeholder);
            return this;
        }

        @Override
        public ParserConfigurator<T> useKeyBasedPlaceholderIndexing() {
            this.useKeyBasedIndexing = true;
            return this;
        }

        protected ParserConfigImpl build() {
            return new ParserConfigImpl(this.tags, new ArrayList<>(this.formats), this.parsePlaceholders, this.tagOpen, this.tagClose, this.useKeyBasedIndexing);
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    class PlaceholderConfiguratorImpl<T> implements PlaceholderConfigurator<T> {
        private final Function<PlaceholderFormatterConfiguratorImpl<?>, PlaceholderFormatterConfiguratorImpl<?>> formatter = UnaryOperator.identity();
        private final Function<PlaceholderDefaultContextConfiguratorImpl<?>, PlaceholderDefaultContextConfiguratorImpl<?>> defaultContext = UnaryOperator.identity();

        @Override
        public PlaceholderConfigurator<T> withFormatter(@NonNull Consumer<PlaceholderFormatterConfigurator<?>> consumer) {
            this.formatter.andThen(formatter -> {
                consumer.accept(formatter);
                return formatter;
            });

            return this;
        }

        @Override
        public PlaceholderConfigurator<T> withDefaultContext(@NonNull Consumer<PlaceholderDefaultContextConfigurator<?>> consumer) {
            this.defaultContext.andThen(defaultContext -> {
                consumer.accept(defaultContext);
                return defaultContext;
            });
            return this;
        }

        protected PlaceholderConfigImpl build() {
            final PlaceholderFormatterConfiguratorImpl<?> formatterConfigurator = this.formatter.apply(new PlaceholderFormatterConfiguratorImpl<>());
            final PlaceholderDefaultContextConfiguratorImpl<?> contextConfigurator = this.defaultContext.apply(new PlaceholderDefaultContextConfiguratorImpl<>());
            return new PlaceholderConfigImpl(
                    formatterConfigurator.defaultFormatters,
                    contextConfigurator.defaultContext
            );
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    class PlaceholderFormatterConfiguratorImpl<T> implements PlaceholderFormatterConfigurator<T> {
        private BlixxFormatters defaultFormatters;

        @Override
        public PlaceholderFormatterConfigurator<T> withStandard() {
            this.defaultFormatters = BlixxDefaultFormatters.getDefault();
            return this;
        }

        @Override
        public PlaceholderFormatterConfigurator<T> withFormatters(BlixxFormatters formatters) {
            this.defaultFormatters = formatters;
            return this;
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    class PlaceholderDefaultContextConfiguratorImpl<T> implements PlaceholderDefaultContextConfigurator<T> {
        private final PlaceholderContextImpl defaultContext = new PlaceholderContextImpl();

        @Override
        public <C> PlaceholderDefaultContextConfigurator<T> withDefaultInheritanceContext(@NonNull C instance) {
            this.defaultContext.registerForHierarchy(instance);
            return this;
        }

        @Override
        public <C> PlaceholderDefaultContextConfigurator<T> withDefaultContext(@NonNull C instance) {
            this.defaultContext.register(instance);
            return this;
        }
    }

    @ApiStatus.Internal
    class Impl implements OptionalStep, BlixxBuilder.PlaceholderSelectorPart<Impl>, BlixxBuilder.ParserSelectorPart<Impl> {
        private Function<PlaceholderConfiguratorImpl<Impl>, PlaceholderConfiguratorImpl<Impl>> placeholderConfigurator = UnaryOperator.identity();
        private Function<ParserConfiguratorImpl<Impl>, ParserConfiguratorImpl<Impl>> parserConfigurator = UnaryOperator.identity();

        @Override
        public Blixx build() {
            final ParserConfigImpl parserConfig = this.parserConfigurator.apply(new ParserConfiguratorImpl<>()).build();
            final PlaceholderConfigImpl placeholderConfig = this.placeholderConfigurator.apply(new PlaceholderConfiguratorImpl<>()).build();
            return new BlixxImpl(parserConfig, placeholderConfig);
        }

        @Override
        public Impl withStandardParserConfig() {
            this.parserConfigurator = configurator -> {
                configurator
                        .withStandardPlaceholderFormat()
                        .withStandardTags();
                return configurator;
            };

            return this;
        }

        @Override
        public Impl withStandardParserConfig(@NonNull Consumer<ParserConfigurator<?>> consumer) {
            this.withStandardParserConfig();
            this.parserConfigurator = this.parserConfigurator.andThen((configurator) -> {
                consumer.accept(configurator);
                return configurator;
            });

            return this;
        }

        @Override
        public Impl withCustomParserConfig(@NonNull Consumer<ParserConfigurator<?>> consumer) {
            this.parserConfigurator = this.parserConfigurator.andThen(configurator -> {
                consumer.accept(configurator);
                return configurator;
            });

            return this;
        }

        @Override
        public Impl withStandardPlaceholderConfig() {
            this.placeholderConfigurator = (configurator) -> {
                configurator.withFormatter(PlaceholderFormatterConfigurator::withStandard);
                return configurator;
            };
            return this;
        }

        @Override
        public Impl withStandardPlaceholderConfig(@NonNull Consumer<PlaceholderConfigurator<?>> consumer) {
            this.withStandardPlaceholderConfig();
            this.placeholderConfigurator = this.placeholderConfigurator.andThen(configurator -> {
                consumer.accept(configurator);
                return configurator;
            });
            return this;
        }

        @Override
        public Impl withCustomPlaceholderConfig(@NonNull Consumer<PlaceholderConfigurator<?>> consumer) {
            this.placeholderConfigurator = this.placeholderConfigurator.andThen(configurator -> {
                consumer.accept(configurator);
                return configurator;
            });
            return this;
        }
    }
}
