package dev.oop778.blixx.api.component;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
/** Platform-agnostic RGB color. */
public class BlixxColor {
    private final int value;

    private BlixxColor(int value) {
        this.value = value;
    }

    /** Creates a color from a packed RGB integer. */
    public static BlixxColor of(int rgb) {
        return new BlixxColor(rgb & 0xFFFFFF);
    }

    /** Creates a color from individual RGB channel values (0-255). */
    public static BlixxColor of(int red, int green, int blue) {
        return new BlixxColor(((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
    }

    /** Parses a hex color string (with or without {@code #} prefix, supports 3- and 6-digit forms). */
    public static BlixxColor fromHex(String hex) {
        if (hex.charAt(0) == '#') {
            hex = hex.substring(1);
        }

        if (hex.length() == 3) {
            hex = String.valueOf(hex.charAt(0))
                    + hex.charAt(0)
                    + hex.charAt(1)
                    + hex.charAt(1)
                    + hex.charAt(2)
                    + hex.charAt(2);
        }

        return new BlixxColor(Integer.parseInt(hex, 16));
    }

    public int red() {
        return (this.value >> 16) & 0xFF;
    }

    public int green() {
        return (this.value >> 8) & 0xFF;
    }

    public int blue() {
        return this.value & 0xFF;
    }

    /** Returns this color as a {@code #rrggbb} hex string. */
    public String asHexString() {
        return String.format("#%06x", this.value);
    }

    @Override
    public String toString() {
        return this.asHexString();
    }
}
