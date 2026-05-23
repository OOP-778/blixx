package dev.oop778.blixx;

import static org.junit.jupiter.api.Assertions.*;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import java.util.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PlaceholderContextTest {
    @Nested
    class CreateExact {
        @Test
        void findRegisteredType() {
            PlaceholderContext ctx = PlaceholderContext.create("hello");
            assertTrue(ctx.find(String.class).isPresent());
            assertEquals("hello", ctx.find(String.class).get());
        }

        @Test
        void findMultipleDifferentTypes() {
            PlaceholderContext ctx = PlaceholderContext.create("hello", 42);
            assertEquals("hello", ctx.find(String.class).get());
            assertEquals(42, ctx.find(Integer.class).get());
        }

        @Test
        void findMissingTypeReturnsEmpty() {
            PlaceholderContext ctx = PlaceholderContext.create("hello");
            assertFalse(ctx.find(Integer.class).isPresent());
        }

        @Test
        void emptyContextFindsNothing() {
            PlaceholderContext ctx = PlaceholderContext.create();
            assertFalse(ctx.find(String.class).isPresent());
        }
    }

    @Nested
    class CreateWithInheritance {
        @Test
        void findByExactClass() {
            ArrayList<String> list = new ArrayList<>();
            list.add("test");
            PlaceholderContext ctx = PlaceholderContext.createWithInheritance(list);
            assertTrue(ctx.find(ArrayList.class).isPresent());
        }

        @Test
        void findBySuperclass() {
            ArrayList<String> list = new ArrayList<>();
            PlaceholderContext ctx = PlaceholderContext.createWithInheritance(list);
            assertTrue(ctx.find(AbstractList.class).isPresent());
        }

        @Test
        void findByDirectInterface() {
            ArrayList<String> list = new ArrayList<>();
            PlaceholderContext ctx = PlaceholderContext.createWithInheritance(list);
            assertTrue(ctx.find(List.class).isPresent());
        }

        @Test
        void findBySuperInterface() {
            ArrayList<String> list = new ArrayList<>();
            PlaceholderContext ctx = PlaceholderContext.createWithInheritance(list);
            assertTrue(ctx.find(Collection.class).isPresent());
        }

        @Test
        void exactRegistrationNotFoundBySupertype() {
            PlaceholderContext ctx = PlaceholderContext.create("hello");
            assertFalse(ctx.find(CharSequence.class).isPresent());
        }
    }

    @Nested
    class WithExact {
        @Test
        void addsToChildContext() {
            PlaceholderContext parent = PlaceholderContext.create("hello");
            PlaceholderContext child = parent.withExact(42);

            assertTrue(child.find(Integer.class).isPresent());
            assertTrue(child.find(String.class).isPresent());
        }

        @Test
        void childShadowsParentForSameType() {
            PlaceholderContext parent = PlaceholderContext.create("hello");
            PlaceholderContext child = parent.withExact("world");

            assertEquals("world", child.find(String.class).get());
        }
    }

    @Nested
    class WithInheritance {
        @Test
        void registersWithHierarchy() {
            PlaceholderContext ctx = PlaceholderContext.create().withInheritance(new ArrayList<>());
            assertTrue(ctx.find(List.class).isPresent());
        }
    }

    @Nested
    class Compose {
        @Test
        void firstContextTakesPriority() {
            PlaceholderContext ctx1 = PlaceholderContext.create("first");
            PlaceholderContext ctx2 = PlaceholderContext.create("second");
            PlaceholderContext composed = PlaceholderContext.compose(ctx1, ctx2);

            assertEquals("first", composed.find(String.class).get());
        }

        @Test
        void fallsBackToSecondContext() {
            PlaceholderContext ctx1 = PlaceholderContext.create(42);
            PlaceholderContext ctx2 = PlaceholderContext.create("hello");
            PlaceholderContext composed = PlaceholderContext.compose(ctx1, ctx2);

            assertEquals(42, composed.find(Integer.class).get());
            assertEquals("hello", composed.find(String.class).get());
        }

        @Test
        void findAllCollectsFromAllContexts() {
            PlaceholderContext ctx1 = PlaceholderContext.create("first");
            PlaceholderContext ctx2 = PlaceholderContext.create("second");
            PlaceholderContext composed = PlaceholderContext.compose(ctx1, ctx2);

            List<String> all = composed.findAll(String.class);
            assertEquals(2, all.size());
            assertTrue(all.contains("first"));
            assertTrue(all.contains("second"));
        }

        @Test
        void findAllDeduplicatesByIdentity() {
            String shared = "shared";
            PlaceholderContext ctx1 = PlaceholderContext.create(shared);
            PlaceholderContext ctx2 = PlaceholderContext.create(shared);
            PlaceholderContext composed = PlaceholderContext.compose(ctx1, ctx2);

            List<String> all = composed.findAll(String.class);
            assertEquals(1, all.size());
        }

        @Test
        void composeWithCollection() {
            List<PlaceholderContext> contexts =
                    Arrays.asList(PlaceholderContext.create("hello"), PlaceholderContext.create(42));
            PlaceholderContext composed = PlaceholderContext.compose(contexts);

            assertTrue(composed.find(String.class).isPresent());
            assertTrue(composed.find(Integer.class).isPresent());
        }

        @Test
        void composedWithExactCreatesNewComposed() {
            PlaceholderContext ctx1 = PlaceholderContext.create("hello");
            PlaceholderContext ctx2 = PlaceholderContext.create(42);
            PlaceholderContext composed = PlaceholderContext.compose(ctx1, ctx2);

            PlaceholderContext extended = composed.withExact(3.14);
            assertTrue(extended.find(Double.class).isPresent());
            assertTrue(extended.find(String.class).isPresent());
        }
    }

    @Nested
    class WithMethod {
        @Test
        void composesCorrectly() {
            PlaceholderContext base = PlaceholderContext.create("base");
            PlaceholderContext added = PlaceholderContext.create(42);
            PlaceholderContext result = base.with(added);

            assertTrue(result.find(String.class).isPresent());
            assertTrue(result.find(Integer.class).isPresent());
        }

        @Test
        void addedContextTakesPriority() {
            PlaceholderContext base = PlaceholderContext.create("base");
            PlaceholderContext added = PlaceholderContext.create("override");
            PlaceholderContext result = base.with(added);

            assertEquals("override", result.find(String.class).get());
        }
    }
}
