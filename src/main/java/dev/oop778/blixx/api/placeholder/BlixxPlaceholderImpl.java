package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;

@ApiStatus.Internal
public class BlixxPlaceholderImpl {
    @RequiredArgsConstructor
    public static class Literal<T> implements BlixxPlaceholder.Literal<T> {
        private final List<String> keys;
        private final Supplier<T> supplier;

        @Override
        public String key() {
            return this.keys.get(0);
        }

        @Override
        public Collection<String> keys() {
            return this.keys;
        }

        @Override
        public T get(@UnknownNullability PlaceholderContext context) {
            return this.supplier.get();
        }
    }

    @RequiredArgsConstructor
    public static class Pattern<T> implements BlixxPlaceholder.Pattern<T> {
        private final java.util.regex.Pattern pattern;
        private final Function<Matcher, T> function;

        @Override
        public java.util.regex.Pattern pattern() {
            return this.pattern;
        }

        @Override
        public T get(@NonNull PlaceholderContext context) {
            final Matcher matcher = context.find(Matcher.class).orElseThrow(() -> new IllegalStateException("Matcher not supplied in Pattern placeholder"));
            return this.function.apply(matcher);
        }
    }

    @RequiredArgsConstructor
    public abstract static class Contextual<T> implements BlixxPlaceholder.Contextual<T> {
        protected final PlaceholderContext defaultContext;
        protected final Class<?>[] requiredClasses;

        protected PlaceholderContext mergeContext(PlaceholderContext with) {
            return with == null ? this.defaultContext : this.defaultContext == null ? with : PlaceholderContext.compose(
                    this.defaultContext,
                    with);
        }

        protected void preconditions(PlaceholderContext context) {
            for (final Class<?> requiredClass : this.requiredClasses) {
                if (requiredClass != PlaceholderContext.class) {
                    if (!context.find(requiredClass).isPresent()) {
                        throw new IllegalStateException(String.format("Failed to find required class - %s - in the context", requiredClass.getName()));
                    }
                }
            }
        }
    }

    public static class ContextualLiteral<T> extends Contextual<T> implements BlixxPlaceholder.ContextualLiteral<T> {
        private final List<String> keys;
        private final Function<PlaceholderContext, T> function;

        public ContextualLiteral(List<String> keys, Function<PlaceholderContext, T> function, PlaceholderContext defaultContext, Class<?>[] requiredClasses) {
            super(defaultContext, requiredClasses);
            this.keys = keys;
            this.function = function;
        }

        @Override
        public T get(@UnknownNullability PlaceholderContext context) {
            context = this.mergeContext(context);
            this.preconditions(context);

            return this.function.apply(context);
        }

        @Override
        public String key() {
            return this.keys.get(0);
        }

        @Override
        public Collection<String> keys() {
            return this.keys;
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static class ContextualPattern<T> extends Contextual<T> implements BlixxPlaceholder.ContextualPattern<T> {
        private final java.util.regex.Pattern pattern;
        private final BiFunction<PlaceholderContext, Matcher, T> function;

        public ContextualPattern(java.util.regex.Pattern pattern, BiFunction<PlaceholderContext, Matcher, T> function, PlaceholderContext defaultContext, Class<?>[] requiredClasses) {
            super(defaultContext, requiredClasses);
            this.pattern = pattern;
            this.function = function;
        }

        @Override
        public T get(@NonNull PlaceholderContext context) {
            context = this.mergeContext(context);
            this.preconditions(context);

            final Matcher matcher = context.find(Matcher.class).orElseThrow(() -> new IllegalStateException("Matcher not supplied in Pattern placeholder"));
            return this.function.apply(context, matcher);
        }
    }
}
