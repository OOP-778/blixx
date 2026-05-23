package dev.oop778.blixx.api.spi;

import java.util.ServiceLoader;

public final class BlixxFactoryHolder {
    private static volatile BlixxFactory INSTANCE;

    public static BlixxFactory get() {
        if (INSTANCE == null) {
            synchronized (BlixxFactoryHolder.class) {
                if (INSTANCE == null) {
                    INSTANCE = ServiceLoader.load(BlixxFactory.class).iterator().next();
                }
            }
        }
        return INSTANCE;
    }

    private BlixxFactoryHolder() {}
}
