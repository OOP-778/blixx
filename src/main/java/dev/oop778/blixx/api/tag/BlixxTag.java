package dev.oop778.blixx.api.tag;

import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public interface BlixxTag<DATA> {
    default DATA createData(BlixxProcessor.@NonNull Context context, @NotNull BaseArgumentQueue args) {
        return null;
    }

    BlixxProcessor getProcessor();

    default boolean compare(BlixxTag<?> other) {
        final BlixxTag<DATA> originalTag = this instanceof Wrapping<?> ? ((Wrapping<DATA>) this).getOriginalTag() : this;
        final BlixxTag<?> otherTag = other instanceof Wrapping<?> ? ((Wrapping<?>) other).getOriginalTag() : other;
        return originalTag.equals(otherTag);
    }

    default boolean canCoexist(@NonNull BlixxProcessor.Context context, @NonNull BlixxTag<?> other) {
        return true;
    }

    default boolean isInstanceOf(Class<?> clazz) {
        return clazz.isInstance(this);
    }

    interface NoData extends BlixxTag<Void> {
    }

    interface Wrapping<T> extends BlixxTag<T> {
        BlixxTag<T> getOriginalTag();

        @Override
        default BlixxProcessor getProcessor() {
            return this.getOriginalTag().getProcessor();
        }

        @Override
        default boolean canCoexist(BlixxProcessor.@NonNull Context context, @NonNull BlixxTag<?> other) {
            return this.getOriginalTag().canCoexist(context, other);
        }

        @Override
        default boolean isInstanceOf(Class<?> clazz) {
            return this.getOriginalTag().isInstanceOf(clazz);
        }
    }

    interface WithDefinedData<T> extends BlixxTag<T> {
        @Override
        default T createData(BlixxProcessor.@NonNull Context context, @NotNull BaseArgumentQueue args) {
            return this.getDefinedData();
        }

        T getDefinedData();

        default boolean compareWithData(@NonNull BlixxTag.WithDefinedData<?> other) {
            return this.compare(other) && this.getDefinedData().equals(other.getDefinedData());
        }
    }

    interface Pattern<T> extends BlixxTag<T> {
        java.util.regex.Pattern getPattern();

        T createDataOfMatcher(BlixxProcessor.@NonNull Context context, @NonNull Matcher matcher);
    }
}
