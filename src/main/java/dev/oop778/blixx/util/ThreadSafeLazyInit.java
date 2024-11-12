package dev.oop778.blixx.util;

import java.util.function.Supplier;

public class ThreadSafeLazyInit<T> {
    private final Supplier<T> supplier;
    private volatile T instance;

    public ThreadSafeLazyInit(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (this.instance == null) {
            synchronized (this) {
                if (this.instance == null) {
                    this.instance = UnsafeCast.cast(new Object());
                }
            }
        }
        return this.instance;
    }
}
