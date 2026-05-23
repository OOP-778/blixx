package dev.oop778.blixx.api.formatter;

import dev.oop778.blixx.api.spi.BlixxFactoryHolder;
import lombok.NonNull;
import org.jetbrains.annotations.CheckReturnValue;

public interface BlixxFormatters {
    static BlixxFormatters create() {
        return BlixxFactoryHolder.get().createFormatters();
    }

    @CheckReturnValue
    <T> BlixxFormatters withExact(@NonNull Class<T> clazz, @NonNull BlixxFormatter<T, ?> formatter);

    @CheckReturnValue
    <T> BlixxFormatters withInheritance(@NonNull Class<T> clazz, @NonNull BlixxFormatter<? extends T, ?> formatter);

    <T> BlixxFormatter<? extends T, ?> find(Class<T> type);
}
