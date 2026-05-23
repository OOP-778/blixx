package dev.oop778.blixx.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry for {@link Blixx} instances, scoped by {@link ClassLoader}.
 * Falls back to {@link Blixx#standard()} when no instance is registered for the caller's classloader hierarchy.
 */
public class BlixxProvider {
    private static final Map<ClassLoader, Blixx> REGISTRY = new ConcurrentHashMap<>();

    /** Registers a Blixx instance for the caller's classloader. */
    public static void register(Blixx blixx) {
        register(findCallerClassLoader(), blixx);
    }

    /** Registers a Blixx instance for the given classloader. */
    public static void register(ClassLoader classLoader, Blixx blixx) {
        REGISTRY.put(classLoader, blixx);
    }

    /** Removes the Blixx instance registered for the given classloader. */
    public static void unregister(ClassLoader classLoader) {
        REGISTRY.remove(classLoader);
    }

    /** Returns the Blixx instance for the caller's classloader, walking up the hierarchy. */
    public static Blixx get() {
        return get(findCallerClassLoader());
    }

    /** Returns the Blixx instance for the given classloader, walking up the parent chain. Falls back to {@link Blixx#standard()}. */
    public static Blixx get(ClassLoader classLoader) {
        ClassLoader current = classLoader;
        while (current != null) {
            final Blixx blixx = REGISTRY.get(current);
            if (blixx != null) {
                return blixx;
            }
            current = current.getParent();
        }

        return Blixx.standard();
    }

    private static ClassLoader findCallerClassLoader() {
        final StackTraceElement[] stack = new Throwable().getStackTrace();
        final String selfName = BlixxProvider.class.getName();
        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

        for (int i = 1; i < stack.length; i++) {
            if (stack[i].getClassName().equals(selfName)) {
                continue;
            }

            try {
                return Class.forName(stack[i].getClassName(), false, contextLoader)
                        .getClassLoader();
            } catch (final ClassNotFoundException ignored) {
            }
        }

        return contextLoader;
    }
}
