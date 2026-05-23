package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.util.UnsafeCast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class BlixxPlaceholderBuilderImpl {
    public static class SelectorStageImpl<BUILD_TARGET, T>
            implements BlixxPlaceholderBuilder.SelectorStage<BUILD_TARGET, T> {
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
        public BlixxPlaceholderBuilder.ContextSelectorStage<BUILD_TARGET, T> contextual() {
            return new ContextualSelectorStageImpl<>(this.postBuildFunction);
        }

        @Override
        public BlixxPlaceholderBuilder.KeyStage<
                        BlixxPlaceholderBuilder.LiteralValueStage<
                                BlixxPlaceholderBuilder.LiteralBuildStage<
                                        BUILD_TARGET, BlixxPlaceholderBuilder.LiteralBuildStage<BUILD_TARGET, ?>>,
                                T>>
                literal() {
            return UnsafeCast.cast(this.supplier.get());
        }

        @Override
        public BlixxPlaceholderBuilder.PatternStage<
                        BlixxPlaceholderBuilder.PatternValueStage<
                                BlixxPlaceholderBuilder.BuildStage<
                                        BUILD_TARGET, BlixxPlaceholderBuilder.BuildStage<BUILD_TARGET, ?>>,
                                T>>
                pattern() {
            return UnsafeCast.cast(this.supplier.get());
        }
    }

    @RequiredArgsConstructor
    public static class ContextualSelectorStageImpl<BUILD_TARGET, T>
            implements BlixxPlaceholderBuilder.ContextSelectorStage<BUILD_TARGET, T> {
        private final Function<BlixxPlaceholder<?>, BUILD_TARGET> postBuildFunction;

        @Override
        public BlixxPlaceholderBuilder.SimpleTypeSelector<
                        BlixxPlaceholderBuilder.ContextualLiteralValueStage<
                                BlixxPlaceholderBuilder.LiteralContextualBuildStage<
                                        BUILD_TARGET, PlaceholderContext, ?>,
                                PlaceholderContext,
                                T>,
                        BlixxPlaceholderBuilder.ContextualPatternValueStage<
                                BlixxPlaceholderBuilder.LiteralContextualBuildStage<
                                        BUILD_TARGET, PlaceholderContext, ?>,
                                PlaceholderContext,
                                T>>
                withMultiple(Class<?>... requiredClasses) {
            return UnsafeCast.cast(new SelectorStageImpl<>(
                    () -> new ContextualBuilderImpl<>(this.postBuildFunction, requiredClasses)));
        }

        @Override
        public <CONTEXT>
                BlixxPlaceholderBuilder.SimpleTypeSelector<
                                BlixxPlaceholderBuilder.ContextualLiteralValueStage<
                                        BlixxPlaceholderBuilder.LiteralContextualBuildStage<BUILD_TARGET, CONTEXT, ?>,
                                        CONTEXT,
                                        T>,
                                BlixxPlaceholderBuilder.ContextualPatternValueStage<
                                        BlixxPlaceholderBuilder.ContextBuildStage<BUILD_TARGET, CONTEXT, ?>,
                                        CONTEXT,
                                        T>>
                        withExact(@NonNull Class<CONTEXT> requiredClass) {
            return UnsafeCast.cast(new SelectorStageImpl<>(
                    () -> new ContextualBuilderImpl<>(this.postBuildFunction, new Class[] {requiredClass})));
        }
    }

    public static class ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T>
            extends BuilderImpl<BUILD_TARGET, ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T>, T>
            implements BlixxPlaceholderBuilder.ContextualPatternValueStage<
                            ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T>, CONTEXT, T>,
                    BlixxPlaceholderBuilder.LiteralContextualBuildStage<
                            BUILD_TARGET, CONTEXT, ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T>> {
        private final Class<?>[] requiredClasses;
        private PlaceholderContext defaultContext;
        private Function<CONTEXT, T> contextSupplier;
        private BiFunction<CONTEXT, Matcher, T> contextAndMatcherSupplier;

        public ContextualBuilderImpl(
                Function<BlixxPlaceholder<?>, BUILD_TARGET> postBuildFunction, Class<?>[] requiredClasses) {
            super(postBuildFunction);
            this.requiredClasses = requiredClasses;
        }

        @Override
        public ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T> withDefaultContext(PlaceholderContext context) {
            this.defaultContext = context;
            return this;
        }

        @Override
        public ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T> withContextAndMatcherSupplying(
                @NonNull BiFunction<CONTEXT, Matcher, T> contextAndMatcherSupplier) {
            this.contextAndMatcherSupplier = contextAndMatcherSupplier;
            return this;
        }

        @Override
        public ContextualBuilderImpl<BUILD_TARGET, CONTEXT, T> withContextSupplying(
                @NonNull Function<CONTEXT, T> contextSupplier) {
            this.contextSupplier = contextSupplier;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected BlixxPlaceholder<?> buildPlaceholder() {
            if (this.pattern != null) {
                return new BlixxPlaceholderImpl.ContextualPattern<>(
                        this.pattern, this.createPatternFunction(), this.defaultContext, this.requiredClasses);
            }

            return new BlixxPlaceholderImpl.ContextualLiteral<>(
                    new ArrayList<>(this.keys),
                    this.createLiteralFunction(),
                    this.defaultContext,
                    this.requiredClasses);
        }

        @SuppressWarnings("unchecked")
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
                    final CONTEXT foundContext = this.requiredClasses[0] == PlaceholderContext.class
                            ? (CONTEXT) context
                            : (CONTEXT) context.find(this.requiredClasses[0]).get();
                    return getterFunction.apply(foundContext, matcher);
                };
            }

            return (context, matcher) -> getterFunction.apply((CONTEXT) context, matcher);
        }

        @SuppressWarnings("unchecked")
        protected Function<PlaceholderContext, Object> createLiteralFunction() {
            final Function<CONTEXT, T> getterFunction;
            if (this.contextSupplier != null) {
                getterFunction = this.contextSupplier;
            } else {
                getterFunction = ($) -> this.valueSupplier.get();
            }

            if (this.requiredClasses.length == 1) {
                return (context) -> {
                    final CONTEXT foundContext = this.requiredClasses[0] == PlaceholderContext.class
                            ? (CONTEXT) context
                            : (CONTEXT) context.find(this.requiredClasses[0]).get();
                    return getterFunction.apply(foundContext);
                };
            }

            return (context) -> getterFunction.apply((CONTEXT) context);
        }
    }

    @RequiredArgsConstructor
    @SuppressWarnings("unchecked")
    public static class BuilderImpl<BUILD_TARGET, B extends BuilderImpl<BUILD_TARGET, ?, T>, T>
            implements BlixxPlaceholderBuilder.LiteralBuildStage<BUILD_TARGET, B>,
                    BlixxPlaceholderBuilder.PatternStage<B>,
                    BlixxPlaceholderBuilder.PatternValueStage<B, T>,
                    BlixxPlaceholderBuilder.KeyStage<B> {
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
                return new BlixxPlaceholderImpl.Pattern<>(
                        this.pattern,
                        this.valueSupplier != null ? ($) -> this.valueSupplier.get() : this.matcherObjectFunction);
            }

            if (!this.keys.isEmpty()) {
                return new BlixxPlaceholderImpl.Literal<>(
                        this.keys.size() == 1
                                ? Collections.singletonList(this.keys.toArray(new String[0])[0])
                                : new ArrayList<>(this.keys),
                        this.valueSupplier,
                        this.constant);
            }

            throw new IllegalStateException("Idk");
        }
    }
}
