package dev.oop778.blixx.api.component;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
/** Platform-agnostic text decoration (bold, italic, underlined, strikethrough, obfuscated). */
public class BlixxDecoration {
    private final String name;

    private BlixxDecoration(String name) {
        this.name = name;
    }

    public static BlixxDecoration of(String name) {
        return new BlixxDecoration(name.toLowerCase());
    }

    @Override
    public String toString() {
        return this.name;
    }
}
