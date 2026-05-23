package dev.oop778.blixx;

import static org.junit.jupiter.api.Assertions.*;

import dev.oop778.blixx.api.formatter.BlixxDefaultFormatters;
import dev.oop778.blixx.api.formatter.BlixxFormatter;
import dev.oop778.blixx.api.formatter.BlixxFormatters;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlixxFormattersTest {
    @Nested
    class DefaultFormatters {
        @Test
        void integerFormatsToString() {
            BlixxFormatter formatter = BlixxDefaultFormatters.getDefault().find(Integer.class);
            assertNotNull(formatter);
            assertEquals("42", formatter.apply(42));
        }

        @Test
        void doubleStripsTrailingZeros() {
            BlixxFormatter formatter = BlixxDefaultFormatters.getDefault().find(Double.class);
            assertNotNull(formatter);
            assertEquals("3", formatter.apply(3.0));
        }

        @Test
        void doublePreservesDecimalDigits() {
            BlixxFormatter formatter = BlixxDefaultFormatters.getDefault().find(Double.class);
            assertNotNull(formatter);
            assertEquals("3.14", formatter.apply(3.14));
        }

        @Test
        void floatStripsTrailingZeros() {
            BlixxFormatter formatter = BlixxDefaultFormatters.getDefault().find(Float.class);
            assertNotNull(formatter);
            assertEquals("5", formatter.apply(5.0f));
        }

        @Test
        void floatPreservesDecimalDigits() {
            BlixxFormatter formatter = BlixxDefaultFormatters.getDefault().find(Float.class);
            assertNotNull(formatter);
            assertEquals("5.5", formatter.apply(5.5f));
        }

        @Test
        void booleanTrueFormatsToEnabled() {
            BlixxFormatter formatter = BlixxDefaultFormatters.getDefault().find(Boolean.class);
            assertNotNull(formatter);
            assertEquals("Enabled", formatter.apply(true));
        }

        @Test
        void booleanFalseFormatsToDisabled() {
            BlixxFormatter formatter = BlixxDefaultFormatters.getDefault().find(Boolean.class);
            assertNotNull(formatter);
            assertEquals("Disabled", formatter.apply(false));
        }

        @Test
        void numberFormatterFoundByBaseClass() {
            BlixxFormatter formatter = BlixxDefaultFormatters.getDefault().find(Number.class);
            assertNotNull(formatter);
            assertEquals("42", formatter.apply(42));
        }
    }

    @Nested
    class CustomFormatters {
        @Test
        void withExactRegistersAndFinds() {
            BlixxFormatters formatters = BlixxFormatters.create().withExact(String.class, s -> s.toUpperCase());

            BlixxFormatter formatter = formatters.find(String.class);
            assertNotNull(formatter);
            assertEquals("HELLO", formatter.apply("hello"));
        }

        @Test
        void withExactReturnsDifferentInstance() {
            BlixxFormatters original = BlixxFormatters.create();
            BlixxFormatters withStr = original.withExact(String.class, s -> s.toUpperCase());

            assertNotSame(original, withStr);
            assertNotNull(withStr.find(String.class));
        }

        @Test
        void withInheritanceLooksUpBySubclass() {
            BlixxFormatters formatters = BlixxFormatters.create().withInheritance(Number.class, n -> "NUM:" + n);

            BlixxFormatter formatter = formatters.find(Integer.class);
            assertNotNull(formatter);
            assertEquals("NUM:42", formatter.apply(42));
        }

        @Test
        void findReturnsNullForUnregisteredType() {
            BlixxFormatters formatters = BlixxFormatters.create();
            assertNull(formatters.find(String.class));
        }

        @Test
        void parentFormatterVisibleInChild() {
            BlixxFormatters parent = BlixxFormatters.create().withExact(String.class, s -> "parent:" + s);

            BlixxFormatters child = parent.withExact(Integer.class, i -> "child:" + i);

            assertNotNull(child.find(String.class));
            assertNotNull(child.find(Integer.class));
        }
    }
}
