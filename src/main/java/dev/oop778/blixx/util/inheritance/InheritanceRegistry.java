package dev.oop778.blixx.util.inheritance;

import dev.oop778.blixx.util.UnsafeCast;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

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
        return (VALUE) this.lookForWithHierarchy(key);
    }

    public void registerExact(Class<?> key, T value) {
        this.inheritanceMap.put(key, value);
    }

    public void registerWithInheritance(Class<?> key, T value) {
        this.registerAtAllSupertypes(key, value);
    }

    private void registerAtAllSupertypes(Class<?> clazz, T value) {
        if (clazz == null || clazz == Object.class) {
            return;
        }

        this.inheritanceMap.put(clazz, value);

        for (final Class<?> iface : clazz.getInterfaces()) {
            this.registerAtAllSupertypes(iface, value);
        }

        this.registerAtAllSupertypes(clazz.getSuperclass(), value);
    }

    private T lookForWithHierarchy(Class<?> key) {
        if (key == null) {
            return null;
        }

        T result = this.lookForExact(key);
        if (result != null) {
            return result;
        }

        result = this.lookForWithHierarchy(key.getSuperclass());
        if (result != null) {
            return result;
        }

        for (final Class<?> iface : key.getInterfaces()) {
            result = this.lookForWithHierarchy(iface);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private T lookForExact(Class<?> key) {
        InheritanceRegistry<T> current = this;
        while (current != null) {
            final T value = (T) current.inheritanceMap.get(key);
            if (value != null) {
                return value;
            }

            current = current.parent;
        }

        return null;
    }
}
