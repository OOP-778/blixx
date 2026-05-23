package dev.oop778.blixx.api.formatter;

import java.util.function.Function;

/**
 * Converts a value of type {@code T} into an output representation.
 *
 * @param <T>      the input type
 * @param <OUTPUT> the output type
 */
@FunctionalInterface
public interface BlixxFormatter<T, OUTPUT> extends Function<T, OUTPUT> {
    /** Convenience specialization that formats to {@link java.lang.String}. */
    interface String<T> extends BlixxFormatter<T, java.lang.String> {}
}
