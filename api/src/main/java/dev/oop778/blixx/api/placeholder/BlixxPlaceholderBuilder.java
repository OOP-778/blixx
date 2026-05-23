package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;

public interface BlixxPlaceholderBuilder {
    interface SelectorStage<BUILD_TARGET, T>
            extends SimpleTypeSelector<
                    LiteralValueStage<LiteralBuildStage<BUILD_TARGET, LiteralBuildStage<BUILD_TARGET, ?>>, T>,
                    PatternValueStage<BuildStage<BUILD_TARGET, BuildStage<BUILD_TARGET, ?>>, T>> {
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
        SimpleTypeSelector<
                        ContextualLiteralValueStage<
                                LiteralContextualBuildStage<BUILD_TARGET, PlaceholderContext, ?>,
                                PlaceholderContext,
                                T>,
                        ContextualPatternValueStage<
                                LiteralContextualBuildStage<BUILD_TARGET, PlaceholderContext, ?>,
                                PlaceholderContext,
                                T>>
                withMultiple(Class<?>... requiredClasses);

        <CONTEXT>
                SimpleTypeSelector<
                                ContextualLiteralValueStage<
                                        LiteralContextualBuildStage<BUILD_TARGET, CONTEXT, ?>, CONTEXT, T>,
                                ContextualPatternValueStage<ContextBuildStage<BUILD_TARGET, CONTEXT, ?>, CONTEXT, T>>
                        withExact(@NonNull Class<CONTEXT> requiredClass);
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

    interface ContextualPatternValueStage<NEXT_STAGE, CONTEXT, T>
            extends ContextualLiteralValueStage<NEXT_STAGE, CONTEXT, T>, PatternStage<NEXT_STAGE> {
        NEXT_STAGE withContextAndMatcherSupplying(@NonNull BiFunction<CONTEXT, Matcher, T> contextAndMatcherSupplier);
    }

    interface BuildStage<BUILD_TARGET, SELF extends BuildStage<BUILD_TARGET, ?>> {
        BUILD_TARGET build();
    }

    interface LiteralBuildStage<BUILD_TARGET, SELF extends LiteralBuildStage<BUILD_TARGET, ?>>
            extends BuildStage<BUILD_TARGET, SELF> {
        SELF withPossibleKey(@org.intellij.lang.annotations.Pattern("[a-zA-Z_0-9]+") String key);
    }

    interface ContextBuildStage<BUILD_TARGET, CONTEXT, SELF extends ContextBuildStage<BUILD_TARGET, CONTEXT, ?>>
            extends BuildStage<BUILD_TARGET, SELF> {
        SELF withDefaultContext(PlaceholderContext context);
    }

    interface LiteralContextualBuildStage<
                    BUILD_TARGET, CONTEXT, SELF extends LiteralContextualBuildStage<BUILD_TARGET, CONTEXT, ?>>
            extends ContextBuildStage<BUILD_TARGET, CONTEXT, SELF>, LiteralBuildStage<BUILD_TARGET, SELF> {}
}
