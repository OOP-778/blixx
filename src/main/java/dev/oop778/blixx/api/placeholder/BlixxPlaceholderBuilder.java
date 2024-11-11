package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.util.UnsafeCast;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface BlixxPlaceholderBuilder {
    interface SelectorStage<BUILD_TARGET, T> extends SimpleTypeSelector<LiteralValueStage<LiteralBuildStage<BUILD_TARGET, LiteralBuildStage<BUILD_TARGET, ?>>, T>, PatternValueStage<BuildStage<BUILD_TARGET, BuildStage<BUILD_TARGET, ?>>, T>> {
        ContextSelectorStage<BUILD_TARGET, T> contextual();
    }

    interface SimpleTypeSelector<LITERAL_NEXT_STAGE, PATTERN_NEXT_STAGE> {
        KeyStage<LITERAL_NEXT_STAGE> literal();

        PatternStage<PATTERN_NEXT_STAGE> pattern();
    }

    interface KeyStage<NEXT_STAGE> {
        NEXT_STAGE withKey(@org.intellij.lang.annotations.Pattern("[a-zA-Z_0-9]+") String key);
    }

    interface PatternStage<NEXT_STAGE> {
        NEXT_STAGE withPattern(Pattern pattern);
    }

    interface ContextSelectorStage<BUILD_TARGET, T> {
        SimpleTypeSelector<ContextualLiteralValueStage<LiteralContextualBuildStage<BUILD_TARGET, PlaceholderContext, ?>, PlaceholderContext, T>, ContextualPatternValueStage<LiteralContextualBuildStage<BUILD_TARGET, PlaceholderContext, ?>, PlaceholderContext, T>> withMultiple(Class<?>... requiredClasses);

        <CONTEXT> SimpleTypeSelector<ContextualLiteralValueStage<LiteralContextualBuildStage<BUILD_TARGET, CONTEXT, ?>, CONTEXT, T>, ContextualPatternValueStage<ContextBuildStage<BUILD_TARGET, CONTEXT, ?>, CONTEXT, T>> withExact(@NonNull Class<CONTEXT> requiredClass);
    }

    interface LiteralValueStage<NEXT_STAGE, T> {
        NEXT_STAGE withValue(@NonNull Supplier<T> valueSupplier);

        NEXT_STAGE withValue(@NonNull T value);
    }

    interface PatternValueStage<NEXT_STAGE, T> extends LiteralValueStage<NEXT_STAGE, T> {
        NEXT_STAGE withMatcherSupplying(@NonNull Function<Matcher, T> matcherSupplier);
    }

    interface ContextualLiteralValueStage<NEXT_STAGE, CONTEXT, T> extends LiteralValueStage<NEXT_STAGE, T> {
        NEXT_STAGE withContextSupplying(@NonNull Function<CONTEXT, T> contextSupplier);
    }

    interface ContextualPatternValueStage<NEXT_STAGE, CONTEXT, T> extends ContextualLiteralValueStage<NEXT_STAGE, CONTEXT, T>, PatternStage<NEXT_STAGE> {
        NEXT_STAGE withContextAndMatcherSupplying(@NonNull BiFunction<CONTEXT, Matcher, T> contextAndMatcherSupplier);
    }

    interface BuildStage<BUILD_TARGET, SELF extends BuildStage<BUILD_TARGET, ?>> {
        BUILD_TARGET build();
    }

    interface LiteralBuildStage<BUILD_TARGET, SELF extends LiteralBuildStage<BUILD_TARGET, ?>> extends BuildStage<BUILD_TARGET, SELF> {
        SELF withPossibleKey(@org.intellij.lang.annotations.Pattern("[a-zA-Z_0-9]+") String key);
    }

    interface ContextBuildStage<BUILD_TARGET, CONTEXT, SELF extends ContextBuildStage<BUILD_TARGET, CONTEXT, ?>> extends BuildStage<BUILD_TARGET, SELF> {
        SELF withDefaultContext(PlaceholderContext context);
    }

    interface LiteralContextualBuildStage<BUILD_TARGET, CONTEXT, SELF extends LiteralContextualBuildStage<BUILD_TARGET, CONTEXT, ?>> extends ContextBuildStage<BUILD_TARGET, CONTEXT, SELF>, LiteralBuildStage<BUILD_TARGET, SELF> {
    }

    @ApiStatus.Internal
    class SelectorStageImpl<BUILD_TARGET, T> implements SelectorStage<BUILD_TARGET, T> {
        private final Function<BlixxPlaceholder<?>, BUILD_TARGET> postBuildFunction;
        private final Supplier<BuilderImpl<BUILD_TARGET, ?, T>> supplier;

        public SelectorStageImpl(Function<BlixxPlaceholder<?>, BUILD_TARGET> postBuildFunction) {
            this.supplier = () -> new BuilderImpl<>(postBuildFunction);
            this.postBuildFunction = postBuildFunction;
        }

        public SelectorStageImpl(Supplier<BuilderImpl<BUILD_TARGET, ?, T>> supplier) {
            this.supplier = supplier;
            this.postBuildFunction = null;
        }

        @Override
        public ContextSelectorStage<BUILD_TARGET, T> contextual() {
            return new ContextualSelectorStageImpl<>(this.postBuildFunction);
        }

        @Override
        public KeyStage<LiteralValueStage<LiteralBuildStage<BUILD_TARGET, LiteralBuildStage<BUILD_TARGET, ?>>, T>> literal() {
            return UnsafeCast.cast(this.supplier.get());
        }

        @Override
        public PatternStage<PatternValueStage<BuildStage<BUILD_TARGET, BuildStage<BUILD_TARGET, ?>>, T>> pattern() {
            return UnsafeCast.cast(this.supplier.get());
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    class ContextualSelectorStageImpl<BUILD_TARGET, T> implements ContextSelectorStage<BUILD_TARGET, T> {
        private final Function<BlixxPlaceholder<?>, BUILD_TARGET> postBuildFunction;

        @Override
        public SimpleTypeSelector<ContextualLiteralValueStage<LiteralContextualBuildStage<BUILD_TARGET, PlaceholderContext, ?>, PlaceholderContext, T>, ContextualPatternValueStage<LiteralContextualBuildStage<BUILD_TARGET, PlaceholderContext, ?>, PlaceholderContext, T>> withMultiple(Class<?>... requiredClasses) {
            return UnsafeCast.cast(new SelectorStageImpl<>(() -> new ContextualBuilderImpl<>(this.postBuildFunction, requiredClasses)));
        }

        @Override
        public <CONTEXT> SimpleTypeSelector<ContextualLiteralValueStage<LiteralContextualBuildStage<BUILD_TARGET, CONTEXT, ?>, CONTEXT, T>, ContextualPatternValueStage<ContextBuildStage<BUILD_TARGET, CONTEXT, ?>, CONTEXT, T>> withExact(@NonNull Class<CONTEXT> requiredClass) {
            return UnsafeCast.cast(new SelectorStageImpl<>(() -> new ContextualBuilderImpl<>(this.postBuildFunction, new Class[]{requiredClass})));
        }
    }

    @ApiStatus.Internal
    class ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T> extends BuilderImpl<BUILD_TARGET, ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T>, T> implements ContextualPatternValueStage<ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T>, CONTEXT, T>, LiteralContextualBuildStage<BUILD_TARGET, CONTEXT, ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T>> {
        private final Class<?>[] requiredClasses;
        private PlaceholderContext defaultContext;
        private Function<CONTEXT, T> contextSupplier;
        private BiFunction<CONTEXT, Matcher, T> contextAndMatcherSupplier;

        public ContextualBuilderImpl(Function<BlixxPlaceholder<?>, BUILD_TARGET> postBuildFunction, Class<?>[] requiredClasses) {
            super(postBuildFunction);
            this.requiredClasses = requiredClasses;
        }

        @Override
        public ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T> withDefaultContext(PlaceholderContext context) {
            this.defaultContext = context;
            return this;
        }

        @Override
        public ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T> withContextAndMatcherSupplying(@NonNull BiFunction<CONTEXT, Matcher, T> contextAndMatcherSupplier) {
            this.contextAndMatcherSupplier = contextAndMatcherSupplier;
            return this;
        }

        @Override
        public ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T> withContextSupplying(@NonNull Function<CONTEXT, T> contextSupplier) {
            this.contextSupplier = contextSupplier;
            return this;
        }

        @Override
        protected BlixxPlaceholder<?> buildPlaceholder() {
            if (this.pattern != null) {
                return new BlixxPlaceholderImpl.ContextualPattern<>(this.pattern, this.createPatternFunction(), this.defaultContext, this.requiredClasses);
            }

            return new BlixxPlaceholderImpl.ContextualLiteral<>(new ArrayList<>(this.keys), this.createLiteralFunction(), this.defaultContext, this.requiredClasses);
        }

        protected BiFunction<PlaceholderContext, Matcher, Object> createPatternFunction() {
            final BiFunction<CONTEXT, Matcher, T> getterFunction;
            if (this.contextAndMatcherSupplier != null) {
                getterFunction = this.contextAndMatcherSupplier;
            } else if (this.contextSupplier != null) {
                getterFunction = (context, $) -> this.contextSupplier.apply(context);
            } else {
                getterFunction = ($, $1) -> this.valueSupplier.get();
            }

            if (this.requiredClasses.length == 1) {
                return (context, matcher) -> {
                    // We know it's safe cause call in placeholder#get() already checks if it exists
                    final CONTEXT foundContext = this.requiredClasses[0] == PlaceholderContext.class ? (CONTEXT) context : (CONTEXT) context.find(this.requiredClasses[0]).get();
                    return getterFunction.apply(foundContext, matcher);
                };
            }

            // Otherwise we know that the context is PlaceholderContext, cause multiple uses that as CONTEXT
            return (context, matcher) -> getterFunction.apply((CONTEXT) context, matcher);
        }

        protected Function<PlaceholderContext, Object> createLiteralFunction() {
            final Function<CONTEXT, T> getterFunction;
            if (this.contextSupplier != null) {
                getterFunction = this.contextSupplier;
            } else {
                getterFunction = ($) -> this.valueSupplier.get();
            }

            if (this.requiredClasses.length == 1) {
                return (context) -> {
                    // We know it's safe cause call in placeholder#get() already checks if it exists
                    final CONTEXT foundContext = this.requiredClasses[0] == PlaceholderContext.class ? (CONTEXT) context : (CONTEXT) context.find(this.requiredClasses[0]).get();
                    return getterFunction.apply(foundContext);
                };
            }

            // Otherwise we know that the context is PlaceholderContext, cause multiple uses that as CONTEXT
            return (context) -> getterFunction.apply((CONTEXT) context);
        }
    }

    @ApiStatus.Internal
    @RequiredArgsConstructor
    class BuilderImpl<BUILD_TARGET, B extends BuilderImpl<BUILD_TARGET, ?, T>, T> implements LiteralBuildStage<BUILD_TARGET, B>, PatternStage<B>, PatternValueStage<B, T>, KeyStage<B> {
        protected final Set<String> keys = new HashSet<>();
        private final Function<BlixxPlaceholder<?>, BUILD_TARGET> postBuildFunction;
        protected Pattern pattern;
        protected Supplier<T> valueSupplier;
        protected Function<Matcher, T> matcherObjectFunction;
        protected boolean constant;

        @Override
        public B withPossibleKey(String key) {
            this.keys.add(key);
            return (B) this;
        }

        @Override
        public BUILD_TARGET build() {
            return this.postBuildFunction.apply(this.buildPlaceholder());
        }

        @Override
        public B withKey(String key) {
            this.keys.add(key);
            return (B) this;
        }

        @Override
        public B withPattern(Pattern pattern) {
            this.pattern = pattern;
            return (B) this;
        }

        @Override
        public B withMatcherSupplying(@NonNull Function<Matcher, T> matcherSupplier) {
            this.matcherObjectFunction = matcherSupplier;
            return (B) this;
        }

        @Override
        public B withValue(@NonNull Supplier<T> valueSupplier) {
            this.valueSupplier = valueSupplier;
            return (B) this;
        }

        @Override
        public B withValue(@NonNull T value) {
            this.constant = true;
            this.valueSupplier = () -> value;
            return (B) this;
        }

        protected BlixxPlaceholder<?> buildPlaceholder() {
            if (this.pattern != null) {
                return new BlixxPlaceholderImpl.Pattern<>(this.pattern, this.valueSupplier != null ? ($) -> this.valueSupplier.get() : this.matcherObjectFunction);
            }

            if (!this.keys.isEmpty()) {
                return new BlixxPlaceholderImpl.Literal<>(new ArrayList<>(this.keys), this.valueSupplier);
            }

            throw new IllegalStateException("Idk");
        }
    }
}
