package dev.oop778.blixx.api.formatter;

import dev.oop778.blixx.util.inheritance.InheritanceRegistry;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.IdentityHashMap;

public class BlixxDefaultFormatters extends BlixxFormattersImpl {
    private static final BlixxDefaultFormatters INSTANCE = new BlixxDefaultFormatters();

    static {
        BlixxDefaultFormatters.registerDefaultInheritance(Number.class, (BlixxFormatter<? extends Number, String>) number -> {
            if (number instanceof Double || number instanceof Float) {
                return BigDecimal.valueOf(number.doubleValue()).stripTrailingZeros().toPlainString();
            }

            return number.toString();
        });

        BlixxDefaultFormatters.registerDefaultExact(Boolean.class, bool -> bool ? "Enabled" : "Disabled");
    }

    public BlixxDefaultFormatters() {
        super(new InheritanceRegistry<>(() -> new IdentityHashMap<>()));
    }

    public static <T> void registerDefaultExact(Class<T> type, @NonNull BlixxFormatter<T, ?> formatter) {
        INSTANCE.registerExact(type, formatter);
    }

    public static <T> void registerDefaultInheritance(Class<T> type, @NonNull BlixxFormatter<? extends T, ?> formatter) {
        INSTANCE.registerInheritance(type, formatter);
    }

    public static BlixxFormatters getDefault() {
        return INSTANCE;
    }
}
