package dev.oop778.blixx;

import static org.junit.jupiter.api.Assertions.*;

import dev.oop778.blixx.api.component.BlixxColor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlixxColorTest {
    @Nested
    class OfRgb {
        @Test
        void basicValue() {
            BlixxColor color = BlixxColor.of(0xFF0000);
            assertEquals(0xFF0000, color.getValue());
        }

        @Test
        void masksTo24Bit() {
            BlixxColor color = BlixxColor.of(0xFFFFFFFF);
            assertEquals(0xFFFFFF, color.getValue());
        }

        @Test
        void zeroIsBlack() {
            BlixxColor color = BlixxColor.of(0);
            assertEquals(0, color.getValue());
        }
    }

    @Nested
    class OfComponents {
        @Test
        void redGreenBlueRoundTrip() {
            BlixxColor color = BlixxColor.of(170, 187, 204);
            assertEquals(170, color.red());
            assertEquals(187, color.green());
            assertEquals(204, color.blue());
        }

        @Test
        void black() {
            BlixxColor color = BlixxColor.of(0, 0, 0);
            assertEquals(0, color.red());
            assertEquals(0, color.green());
            assertEquals(0, color.blue());
        }

        @Test
        void white() {
            BlixxColor color = BlixxColor.of(255, 255, 255);
            assertEquals(255, color.red());
            assertEquals(255, color.green());
            assertEquals(255, color.blue());
        }

        @Test
        void valuesAbove255GetMasked() {
            BlixxColor color = BlixxColor.of(256, 0, 0);
            assertEquals(0, color.red());
        }
    }

    @Nested
    class FromHex {
        @Test
        void withHash() {
            BlixxColor color = BlixxColor.fromHex("#AABBCC");
            assertEquals(0xAABBCC, color.getValue());
        }

        @Test
        void withoutHash() {
            BlixxColor color = BlixxColor.fromHex("AABBCC");
            assertEquals(0xAABBCC, color.getValue());
        }

        @Test
        void shortHexExpands() {
            BlixxColor color = BlixxColor.fromHex("#abc");
            assertEquals(0xAABBCC, color.getValue());
        }

        @Test
        void lowercase() {
            BlixxColor color = BlixxColor.fromHex("#ff8800");
            assertEquals(0xFF8800, color.getValue());
        }
    }

    @Nested
    class ComponentExtraction {
        @Test
        void redFromHex() {
            BlixxColor color = BlixxColor.fromHex("#FF0000");
            assertEquals(255, color.red());
            assertEquals(0, color.green());
            assertEquals(0, color.blue());
        }

        @Test
        void greenFromHex() {
            BlixxColor color = BlixxColor.fromHex("#00FF00");
            assertEquals(0, color.red());
            assertEquals(255, color.green());
            assertEquals(0, color.blue());
        }

        @Test
        void blueFromHex() {
            BlixxColor color = BlixxColor.fromHex("#0000FF");
            assertEquals(0, color.red());
            assertEquals(0, color.green());
            assertEquals(255, color.blue());
        }
    }

    @Nested
    class HexString {
        @Test
        void formatsWithHashAndPadding() {
            BlixxColor color = BlixxColor.of(0x0000FF);
            assertEquals("#0000ff", color.asHexString());
        }

        @Test
        void fullColorFormats() {
            BlixxColor color = BlixxColor.of(0xAABBCC);
            assertEquals("#aabbcc", color.asHexString());
        }

        @Test
        void toStringIsHex() {
            BlixxColor color = BlixxColor.of(0xFF0000);
            assertEquals("#ff0000", color.toString());
        }
    }

    @Nested
    class Equality {
        @Test
        void sameValueEquals() {
            assertEquals(BlixxColor.of(0xFF0000), BlixxColor.of(0xFF0000));
        }

        @Test
        void differentValueNotEquals() {
            assertNotEquals(BlixxColor.of(0xFF0000), BlixxColor.of(0x00FF00));
        }

        @Test
        void sameHashCode() {
            assertEquals(
                    BlixxColor.of(0xFF0000).hashCode(), BlixxColor.of(0xFF0000).hashCode());
        }

        @Test
        void hexAndComponentsEqual() {
            assertEquals(BlixxColor.fromHex("#AABBCC"), BlixxColor.of(170, 187, 204));
        }
    }
}
