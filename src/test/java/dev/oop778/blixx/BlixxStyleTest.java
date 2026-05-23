package dev.oop778.blixx;

import static org.junit.jupiter.api.Assertions.*;

import dev.oop778.blixx.api.component.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlixxStyleTest {
    @Nested
    class EmptyState {
        @Test
        void newStyleIsEmpty() {
            assertTrue(new BlixxStyle().isEmpty());
        }

        @Test
        void newStyleHasNullColor() {
            assertNull(new BlixxStyle().getColor());
        }

        @Test
        void newStyleHasEmptyDecorations() {
            assertTrue(new BlixxStyle().getDecorations().isEmpty());
        }
    }

    @Nested
    class ColorMethods {
        @Test
        void settingColorMakesNonEmpty() {
            BlixxStyle style = new BlixxStyle().color(BlixxColor.of(0xFF0000));
            assertFalse(style.isEmpty());
            assertEquals(BlixxColor.of(0xFF0000), style.getColor());
        }

        @Test
        void clearingColorRestoresEmpty() {
            BlixxStyle style = new BlixxStyle().color(BlixxColor.of(0xFF0000));
            style.color(null);
            assertTrue(style.isEmpty());
        }

        @Test
        void colorIfAbsentSetsWhenNull() {
            BlixxStyle style = new BlixxStyle().colorIfAbsent(BlixxColor.of(0xFF0000));
            assertEquals(BlixxColor.of(0xFF0000), style.getColor());
        }

        @Test
        void colorIfAbsentDoesNotOverwrite() {
            BlixxStyle style = new BlixxStyle().color(BlixxColor.of(0x00FF00)).colorIfAbsent(BlixxColor.of(0xFF0000));
            assertEquals(BlixxColor.of(0x00FF00), style.getColor());
        }
    }

    @Nested
    class DecorationMethods {
        @Test
        void addingSingleDecoration() {
            BlixxStyle style = new BlixxStyle().decorate(BlixxDecoration.of("bold"));
            assertFalse(style.isEmpty());
            assertTrue(style.getDecorations().contains(BlixxDecoration.of("bold")));
        }

        @Test
        void addingMultipleDecorations() {
            BlixxStyle style =
                    new BlixxStyle().decorate(BlixxDecoration.of("bold")).decorate(BlixxDecoration.of("italic"));
            assertEquals(2, style.getDecorations().size());
        }

        @Test
        void duplicateDecorationIgnored() {
            BlixxStyle style =
                    new BlixxStyle().decorate(BlixxDecoration.of("bold")).decorate(BlixxDecoration.of("bold"));
            assertEquals(1, style.getDecorations().size());
        }
    }

    @Nested
    class EventMethods {
        @Test
        void settingClickEvent() {
            BlixxClickEvent event = BlixxClickEvent.of("open_url", "https://example.com");
            BlixxStyle style = new BlixxStyle().clickEvent(event);
            assertFalse(style.isEmpty());
            assertEquals(event, style.getClickEvent());
        }

        @Test
        void clearingClickEvent() {
            BlixxStyle style = new BlixxStyle()
                    .clickEvent(BlixxClickEvent.of("open_url", "https://example.com"))
                    .clickEvent(null);
            assertNull(style.getClickEvent());
        }

        @Test
        void settingHoverEvent() {
            BlixxStyle style = new BlixxStyle().hoverEvent(null);
            assertTrue(style.isEmpty());
        }
    }

    @Nested
    class OtherProperties {
        @Test
        void settingFont() {
            BlixxStyle style = new BlixxStyle().font("minecraft:uniform");
            assertFalse(style.isEmpty());
            assertEquals("minecraft:uniform", style.getFont());
        }

        @Test
        void settingInsertion() {
            BlixxStyle style = new BlixxStyle().insertion("inserted text");
            assertFalse(style.isEmpty());
            assertEquals("inserted text", style.getInsertion());
        }

        @Test
        void clearingFontAndInsertion() {
            BlixxStyle style =
                    new BlixxStyle().font("test").insertion("test").font(null).insertion(null);
            assertTrue(style.isEmpty());
        }
    }

    @Nested
    class FluentChaining {
        @Test
        void returnsSameInstance() {
            BlixxStyle style = new BlixxStyle();
            assertSame(style, style.color(BlixxColor.of(0xFF0000)));
            assertSame(style, style.colorIfAbsent(BlixxColor.of(0x00FF00)));
            assertSame(style, style.decorate(BlixxDecoration.of("bold")));
            assertSame(style, style.clickEvent(null));
            assertSame(style, style.hoverEvent(null));
            assertSame(style, style.font("test"));
            assertSame(style, style.insertion("test"));
        }
    }
}
