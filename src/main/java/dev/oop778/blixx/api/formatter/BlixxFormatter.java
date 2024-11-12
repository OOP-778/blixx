package dev.oop778.blixx.api.formatter;

import net.kyori.adventure.text.ComponentLike;

import java.util.function.Function;

@FunctionalInterface
public interface BlixxFormatter<T, OUTPUT> extends Function<T, OUTPUT> {
    interface Component<T> extends BlixxFormatter<T, ComponentLike> {
    }

    interface String<T> extends BlixxFormatter<T, java.lang.String> {
    }
}
