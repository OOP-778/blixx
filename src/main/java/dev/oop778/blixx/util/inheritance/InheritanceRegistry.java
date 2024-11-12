package dev.oop778.blixx.util.inheritance;

import dev.oop778.blixx.util.UnsafeCast;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class InheritanceRegistry<T> {
    private final InheritanceRegistry<T> parent;
    private final Supplier<Map<Class<?>, ? super T>> mapFactory;
    private final Map<Class<?>, ? super T> inheritanceMap;

    public InheritanceRegistry(Supplier<Map<Class<?>, ? super T>> mapFactory) {
        this.parent = null;
        this.mapFactory = mapFactory;
        this.inheritanceMap = mapFactory.get();
    }

    public InheritanceRegistry(InheritanceRegistry<T> parent) {
        this.parent = parent;
        this.mapFactory = UnsafeCast.cast(parent.mapFactory);
        this.inheritanceMap = parent.inheritanceMap;
    }

    public InheritanceRegistry<T> createChildren() {
        return new InheritanceRegistry<>(this);
    }

    @Nullable
    public <VALUE extends T> VALUE get(Class<?> key) {
        return (VALUE) this.lookFor(key);
    }

    public void registerExact(Class<?> key, T value) {
        this.inheritanceMap.put(key, value);
    }

    public void registerWithInheritance(Class<?> key, T value) {
        Class<?> clazz = key;

        while (clazz != null && clazz != Object.class) {
            for (final Class<?> iface : value.getClass().getInterfaces()) {
                this.inheritanceMap.put(iface, value);
            }

            this.inheritanceMap.put(clazz, value);
            clazz = clazz.getSuperclass();
        }
    }

    private T lookFor(Class<?> key) {
        InheritanceRegistry<T> current = this;
        while (current != null) {
            final T value = current.get(key);
            if (value != null) {
                return value;
            }

            current = current.parent;
        }

        return null;
    }
}
