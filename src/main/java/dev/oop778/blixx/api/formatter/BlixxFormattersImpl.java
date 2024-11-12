package dev.oop778.blixx.api.formatter;

import dev.oop778.blixx.util.inheritance.InheritanceRegistry;
import lombok.NonNull;

public class BlixxFormattersImpl implements BlixxFormatters {
    private final InheritanceRegistry<BlixxFormatter<?, ?>> registry;

    public BlixxFormattersImpl(InheritanceRegistry<BlixxFormatter<?, ?>> registry) {
        this.registry = registry;
    }

    @Override
    public <T> BlixxFormatters withExact(@NonNull Class<T> clazz, @NonNull BlixxFormatter<T, ?> formatter) {
        final BlixxFormattersImpl copy = new BlixxFormattersImpl(this.registry.createChildren());
        copy.registerExact(clazz, formatter);
        return copy;
    }

    @Override
    public <T> BlixxFormatters withInheritance(@NonNull Class<T> clazz, @NonNull BlixxFormatter<? extends T, ?> formatter) {
        final BlixxFormattersImpl copy = new BlixxFormattersImpl(this.registry.createChildren());
        copy.registerInheritance(clazz, formatter);
        return copy;
    }

    @Override
    public <T> BlixxFormatter<? extends T, ?> find(Class<T> type) {
        return this.registry.get(type);
    }

    protected void registerExact(Class<?> type, BlixxFormatter<?, ?> formatter) {
        this.registry.registerExact(type, formatter);
    }

    protected void registerInheritance(Class<?> type, BlixxFormatter<?, ?> formatter) {
        this.registerInheritance(type, formatter);
    }
}
