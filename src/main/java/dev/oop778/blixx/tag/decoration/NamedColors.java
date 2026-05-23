package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.component.BlixxColor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class NamedColors {
    private static final Map<String, BlixxColor> BY_NAME;

    static {
        final Map<String, BlixxColor> map = new LinkedHashMap<>();
        map.put("black", BlixxColor.of(0x000000));
        map.put("dark_blue", BlixxColor.of(0x0000AA));
        map.put("dark_green", BlixxColor.of(0x00AA00));
        map.put("dark_aqua", BlixxColor.of(0x00AAAA));
        map.put("dark_red", BlixxColor.of(0xAA0000));
        map.put("dark_purple", BlixxColor.of(0xAA00AA));
        map.put("gold", BlixxColor.of(0xFFAA00));
        map.put("gray", BlixxColor.of(0xAAAAAA));
        map.put("dark_gray", BlixxColor.of(0x555555));
        map.put("blue", BlixxColor.of(0x5555FF));
        map.put("green", BlixxColor.of(0x55FF55));
        map.put("aqua", BlixxColor.of(0x55FFFF));
        map.put("red", BlixxColor.of(0xFF5555));
        map.put("light_purple", BlixxColor.of(0xFF55FF));
        map.put("yellow", BlixxColor.of(0xFFFF55));
        map.put("white", BlixxColor.of(0xFFFFFF));
        BY_NAME = Collections.unmodifiableMap(map);
    }

    private NamedColors() {}

    @Nullable
    public static BlixxColor byName(String name) {
        return BY_NAME.get(name.toLowerCase());
    }

    public static Map<String, BlixxColor> all() {
        return BY_NAME;
    }

    @Nullable
    public static String nameOf(BlixxColor color) {
        for (final Map.Entry<String, BlixxColor> entry : BY_NAME.entrySet()) {
            if (entry.getValue().equals(color)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
