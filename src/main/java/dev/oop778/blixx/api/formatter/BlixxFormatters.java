package dev.oop778.blixx.api.formatter;

import dev.oop778.blixx.util.inheritance.InheritanceRegistry;
import lombok.NonNull;
import org.jetbrains.annotations.CheckReturnValue;

import java.util.IdentityHashMap;

public interface BlixxFormatters {

    static BlixxFormatters create() {
        return new BlixxFormattersImpl(new InheritanceRegistry<>(() -> new IdentityHashMap<>()));
    }

    @CheckReturnValue
    <T> BlixxFormatters withExact(@NonNull Class<T> clazz, @NonNull BlixxFormatter<T, ?> formatter);

    @CheckReturnValue
    <T> BlixxFormatters withInheritance(@NonNull Class<T> clazz, @NonNull BlixxFormatter<? extends T, ?> formatter);

    <T> BlixxFormatter<? extends T, ?> find(Class<T> type);
}
