package dev.oop778.blixx.api.tag;

import dev.oop778.blixx.util.StringQueue;
import java.util.regex.Matcher;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * A tag that can be parsed from MiniMessage-style syntax and processed during component building.
 *
 * @param <DATA> the data type this tag produces during parsing
 */
public interface BlixxTag<DATA> {
    /** Creates tag data from parser context and arguments. Returns null by default. */
    default DATA createData(@NonNull BlixxProcessor.@NonNull ParserContext context, @NotNull StringQueue args) {
        return null;
    }

    /** Returns the processor that applies this tag during component building. */
    BlixxProcessor getProcessor();

    /** Checks if this tag is equivalent to another, unwrapping any {@link Wrapping} layers. */
    default boolean compare(BlixxTag<?> other) {
        final BlixxTag<?> thisTag = this instanceof Wrapping ? ((Wrapping<?>) this).getOriginalTag() : this;
        final BlixxTag<?> otherTag = other instanceof Wrapping ? ((Wrapping<?>) other).getOriginalTag() : other;

        if (this instanceof BlixxTag.WithDefinedData && other instanceof BlixxTag.WithDefinedData) {
            final WithDefinedData<?> thisData = (WithDefinedData<?>) this;
            final WithDefinedData<?> otherData = (WithDefinedData<?>) other;

            return thisTag.compare(otherTag) && thisData.getDefinedData().equals(otherData.getDefinedData());
        }

        return thisTag.equals(otherTag);
    }

    default boolean canCoexist(@NonNull BlixxTag<?> other) {
        return true;
    }

    default boolean isInstanceOf(Class<?> clazz) {
        return clazz.isInstance(this);
    }

    /** A tag that carries no data. */
    interface NoData extends BlixxTag<Void> {}

    /** A tag that wraps another tag, delegating processor and coexistence checks. */
    interface Wrapping<T> extends BlixxTag<T> {
        BlixxTag<T> getOriginalTag();

        @Override
        default BlixxProcessor getProcessor() {
            return this.getOriginalTag().getProcessor();
        }

        @Override
        default boolean canCoexist(@NonNull BlixxTag<?> other) {
            return this.getOriginalTag().canCoexist(other);
        }

        @Override
        default boolean isInstanceOf(Class<?> clazz) {
            return this.getOriginalTag().isInstanceOf(clazz);
        }
    }

    /** A tag instance with its data already resolved. */
    interface WithDefinedData<T> extends BlixxTag<T>, BlixxTag.Wrapping<T> {
        @Override
        default T createData(@NonNull BlixxProcessor.ParserContext context, @NotNull StringQueue args) {
            return this.getDefinedData();
        }

        T getDefinedData();

        default boolean compareWithData(@NonNull BlixxTag.WithDefinedData<?> other) {
            return this.compare(other) && this.getDefinedData().equals(other.getDefinedData());
        }
    }

    /** A tag matched by regex pattern rather than exact name. */
    interface Pattern<T> extends BlixxTag<T> {
        java.util.regex.Pattern getPattern();

        T createDataOfMatcher(BlixxProcessor.@NonNull ParserContext context, @NonNull Matcher matcher);
    }
}
