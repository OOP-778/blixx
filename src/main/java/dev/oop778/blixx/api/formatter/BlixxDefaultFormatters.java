package dev.oop778.blixx.api.formatter;

import dev.oop778.blixx.util.inheritance.InheritanceRegistry;
import java.math.BigDecimal;
import java.util.IdentityHashMap;
import lombok.NonNull;

/**
 * Singleton formatter registry with built-in defaults:
 * <ul>
 *   <li>{@link Number} — strips trailing zeros from decimals</li>
 *   <li>{@link Boolean} — formats as "Enabled" / "Disabled"</li>
 * </ul>
 */
public class BlixxDefaultFormatters extends BlixxFormattersImpl {
    private static final BlixxDefaultFormatters INSTANCE = new BlixxDefaultFormatters();

    static {
        BlixxDefaultFormatters.registerDefaultInheritance(
                Number.class, (BlixxFormatter<? extends Number, String>) number -> {
                    if (number instanceof Double || number instanceof Float) {
                        return BigDecimal.valueOf(number.doubleValue())
                                .stripTrailingZeros()
                                .toPlainString();
                    }

                    return number.toString();
                });

        BlixxDefaultFormatters.registerDefaultExact(Boolean.class, bool -> bool ? "Enabled" : "Disabled");
    }

    public BlixxDefaultFormatters() {
        super(new InheritanceRegistry<>(() -> new IdentityHashMap<>()));
    }

    /** Registers a default formatter for an exact type match. */
    public static <T> void registerDefaultExact(Class<T> type, @NonNull BlixxFormatter<T, ?> formatter) {
        INSTANCE.registerExact(type, formatter);
    }

    /** Registers a default formatter that matches the type and all its subtypes. */
    public static <T> void registerDefaultInheritance(
            Class<T> type, @NonNull BlixxFormatter<? extends T, ?> formatter) {
        INSTANCE.registerInheritance(type, formatter);
    }

    /** Returns the shared default formatters instance. */
    public static BlixxFormatters getDefault() {
        return INSTANCE;
    }
}
