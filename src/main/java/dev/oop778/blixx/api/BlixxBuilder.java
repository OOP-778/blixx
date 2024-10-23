package dev.oop778.blixx.api;

import dev.oop778.blixx.api.parser.config.ParserConfigImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.PlaceholderConfigImpl;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContextImpl;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.api.util.Pair;
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

        ParserConfigurator<T> withLegacyColorSupport();

        ParserConfigurator<T> withHexColorSupport();

        ParserConfigurator<T> withStrictMode();
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

        <I> PlaceholderFormatterConfigurator<T> withFormatter(Class<I> clazz, Function<I, Object> formatter);
    }

    interface PlaceholderDefaultContextConfigurator<T> {
        <C> PlaceholderDefaultContextConfigurator<T> withDefaultContext(@NonNull C instance, boolean hierarchy);
    }

    @ApiStatus.Internal
    class ParserConfiguratorImpl<T> implements ParserConfigurator<T> {
        private final Map<String, BlixxTag<?>> tags = new HashMap<>();
        private final Set<Pair<Character, Character>> formats = new HashSet<>();
        private final List<BlixxPlaceholder<String>> parsePlaceholders = new ArrayList<>();
        private boolean supportsLegacyColorCodes;
        private boolean supportsHexColorCodes;
        private boolean strictMode;
        private char tagOpen = '<';
        private char tagClose = '>';

        @Override
        public ParserConfigurator<T> withStandardTags() {
            // Implementation...
            return this;
        }

        @Override
        public ParserConfigurator<T> withStandardPlaceholderFormat() {
            // TODO: Add standard formats
            return this;
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
        public ParserConfigurator<T> withLegacyColorSupport() {
            this.supportsLegacyColorCodes = true;
            return this;
        }

        @Override
        public ParserConfigurator<T> withHexColorSupport() {
            this.supportsHexColorCodes = true;
            return this;
        }

        @Override
        public ParserConfigurator<T> withStrictMode() {
            this.strictMode = true;
            return this;
        }

        protected ParserConfigImpl build() {
            return new ParserConfigImpl(this.tags, new ArrayList<>(this.formats), this.parsePlaceholders, this.tagOpen, this.tagClose, this.supportsLegacyColorCodes, this.supportsHexColorCodes, this.strictMode);
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
                    formatterConfigurator.formatters,
                    contextConfigurator.context
            );
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    class PlaceholderFormatterConfiguratorImpl<T> implements PlaceholderFormatterConfigurator<T> {
        private final Map<Class<?>, Function<?, Object>> formatters = new IdentityHashMap<>();

        @Override
        public PlaceholderFormatterConfigurator<T> withStandard() {
            // TODO: Add standard formatters
            return this;
        }

        @Override
        public <I> PlaceholderFormatterConfigurator<T> withFormatter(Class<I> clazz, Function<I, Object> formatter) {
            this.formatters.put(clazz, formatter);
            return this;
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    class PlaceholderDefaultContextConfiguratorImpl<T> implements PlaceholderDefaultContextConfigurator<T> {
        private final PlaceholderContextImpl context = new PlaceholderContextImpl();

        @Override
        public <C> PlaceholderDefaultContextConfigurator<T> withDefaultContext(@NonNull C instance, boolean hierarchy) {
            if (hierarchy) {
                this.context.registerForHierarchy(instance);
            } else {
                this.context.register(instance);
            }

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
            // TODO: Create standard parser config
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
            // TODO: Add standard placeholder config
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
