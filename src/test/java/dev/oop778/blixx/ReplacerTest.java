package dev.oop778.blixx;

import static org.junit.jupiter.api.Assertions.*;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.composed.ComposedReplacer;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReplacerTest {
    @Nested
    class ImmutableReplacer {
        @Test
        void createImmutableStartsEmpty() {
            Replacer replacer = Replacer.createImmutable();
            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            replacer.getPlaceholders().forEach(placeholders::add);
            assertTrue(placeholders.isEmpty());
        }

        @Test
        void withPlaceholderReturnsNewInstance() {
            Replacer original = Replacer.createImmutable();
            BlixxPlaceholder<Object> placeholder = BlixxPlaceholder.literal("key", "value");
            Replacer modified = original.withPlaceholder(placeholder);

            assertNotSame(original, modified);
        }

        @Test
        void originalUnchangedAfterWithPlaceholder() {
            Replacer original = Replacer.createImmutable();
            original.withPlaceholder(BlixxPlaceholder.literal("key", "value"));

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            original.getPlaceholders().forEach(placeholders::add);
            assertTrue(placeholders.isEmpty());
        }

        @Test
        void withLiteralAddsPlaceholder() {
            Replacer replacer = Replacer.createImmutable().withLiteral("key", "value");

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            replacer.getPlaceholders().forEach(placeholders::add);
            assertEquals(1, placeholders.size());
        }

        @Test
        void withPlaceholderAtStartAddsAtBeginning() {
            BlixxPlaceholder<Object> first = BlixxPlaceholder.literal("first", "1");
            BlixxPlaceholder<Object> second = BlixxPlaceholder.literal("second", "2");

            Replacer replacer = Replacer.createImmutable()
                    .withPlaceholder(first)
                    .withPlaceholder(PlaceholderHolder.Where.START, second);

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            replacer.getPlaceholders().forEach(placeholders::add);
            assertEquals(2, placeholders.size());
            assertSame(second, placeholders.get(0));
        }

        @Test
        void toMutableCreatesMutableCopy() {
            Replacer immutable = Replacer.createImmutable().withLiteral("key", "value");

            MutableReplacer mutable = immutable.toMutable();
            assertNotNull(mutable);

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            mutable.getPlaceholders().forEach(placeholders::add);
            assertEquals(1, placeholders.size());
        }

        @Test
        void getPlaceholdersReturnsUnmodifiable() {
            Replacer replacer = Replacer.createImmutable().withLiteral("key", "value");

            assertThrows(UnsupportedOperationException.class, () -> {
                ((List<BlixxPlaceholder<?>>) replacer.getPlaceholders()).add(BlixxPlaceholder.literal("x", "y"));
            });
        }

        @Test
        void varargFactory() {
            BlixxPlaceholder<Object> p1 = BlixxPlaceholder.literal("a", "1");
            BlixxPlaceholder<Object> p2 = BlixxPlaceholder.literal("b", "2");
            Replacer replacer = Replacer.createImmutable(p1, p2);

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            replacer.getPlaceholders().forEach(placeholders::add);
            assertEquals(2, placeholders.size());
        }

        @Test
        void iterableWorks() {
            Replacer replacer = Replacer.createImmutable().withLiteral("a", "1").withLiteral("b", "2");

            int count = 0;
            for (BlixxPlaceholder<?> ignored : replacer) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Nested
    class MutableReplacerTests {
        @Test
        void createStartsEmpty() {
            MutableReplacer replacer = MutableReplacer.create();
            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            replacer.getPlaceholders().forEach(placeholders::add);
            assertTrue(placeholders.isEmpty());
        }

        @Test
        void withPlaceholderModifiesInPlace() {
            MutableReplacer replacer = MutableReplacer.create();
            MutableReplacer result = replacer.withLiteral("key", "value");

            assertSame(replacer, result);
        }

        @Test
        void withPlaceholderAdds() {
            MutableReplacer replacer = MutableReplacer.create();
            replacer.withLiteral("a", "1");
            replacer.withLiteral("b", "2");

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            replacer.getPlaceholders().forEach(placeholders::add);
            assertEquals(2, placeholders.size());
        }

        @Test
        void toImmutableCreatesIndependentCopy() {
            MutableReplacer mutable = MutableReplacer.create();
            mutable.withLiteral("key", "value");

            Replacer immutable = mutable.toImmutable();

            mutable.withLiteral("another", "value2");

            List<BlixxPlaceholder<?>> immutablePlaceholders = new ArrayList<>();
            immutable.getPlaceholders().forEach(immutablePlaceholders::add);
            assertEquals(1, immutablePlaceholders.size());
        }

        @Test
        void withPlaceholderAtStart() {
            BlixxPlaceholder<Object> first = BlixxPlaceholder.literal("first", "1");
            BlixxPlaceholder<Object> second = BlixxPlaceholder.literal("second", "2");

            MutableReplacer replacer = MutableReplacer.create();
            replacer.withPlaceholder(first);
            replacer.withPlaceholder(PlaceholderHolder.Where.START, second);

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            replacer.getPlaceholders().forEach(placeholders::add);
            assertSame(second, placeholders.get(0));
            assertSame(first, placeholders.get(1));
        }

        @Test
        void varargFactory() {
            BlixxPlaceholder<Object> p1 = BlixxPlaceholder.literal("a", "1");
            BlixxPlaceholder<Object> p2 = BlixxPlaceholder.literal("b", "2");
            MutableReplacer replacer = Replacer.createMutable(p1, p2);

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            replacer.getPlaceholders().forEach(placeholders::add);
            assertEquals(2, placeholders.size());
        }
    }

    @Nested
    class ComposedReplacerTests {
        @Test
        void combinesPlaceholdersFromMultipleHolders() {
            Replacer r1 = Replacer.createImmutable().withLiteral("a", "1");
            Replacer r2 = Replacer.createImmutable().withLiteral("b", "2");

            ComposedReplacer composed = ComposedReplacer.create(r1, r2);

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            composed.getPlaceholders().forEach(placeholders::add);
            assertEquals(2, placeholders.size());
        }

        @Test
        void combinesMutableAndImmutable() {
            Replacer immutable = Replacer.createImmutable().withLiteral("a", "1");
            MutableReplacer mutable = MutableReplacer.create();
            mutable.withLiteral("b", "2");

            ComposedReplacer composed = ComposedReplacer.create(immutable, mutable);

            List<BlixxPlaceholder<?>> placeholders = new ArrayList<>();
            composed.getPlaceholders().forEach(placeholders::add);
            assertEquals(2, placeholders.size());
        }
    }

    @Nested
    class PlaceholderLiteral {
        @Test
        void literalPlaceholderHasKey() {
            BlixxPlaceholder<Object> placeholder = BlixxPlaceholder.literal("player_name", "Steve");
            assertInstanceOf(BlixxPlaceholder.Literal.class, placeholder);
            assertEquals("player_name", ((BlixxPlaceholder.Literal<?>) placeholder).key());
        }

        @Test
        void literalPlaceholderReturnsValue() {
            BlixxPlaceholder<Object> placeholder = BlixxPlaceholder.literal("key", "value");
            assertEquals("value", placeholder.get(null));
        }

        @Test
        void literalPlaceholderKeysContainsKey() {
            BlixxPlaceholder<Object> placeholder = BlixxPlaceholder.literal("mykey", "myvalue");
            BlixxPlaceholder.Literal<?> literal = (BlixxPlaceholder.Literal<?>) placeholder;
            assertTrue(literal.keys().contains("mykey"));
        }
    }
}
