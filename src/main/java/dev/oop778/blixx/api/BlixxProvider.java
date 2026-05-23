package dev.oop778.blixx.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlixxProvider {
    private static final Map<ClassLoader, Blixx> REGISTRY = new ConcurrentHashMap<>();

    public static void register(Blixx blixx) {
        register(findCallerClassLoader(), blixx);
    }

    public static void register(ClassLoader classLoader, Blixx blixx) {
        REGISTRY.put(classLoader, blixx);
    }

    public static void unregister(ClassLoader classLoader) {
        REGISTRY.remove(classLoader);
    }

    public static Blixx get() {
        return get(findCallerClassLoader());
    }

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
