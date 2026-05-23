package dev.oop778.blixx;

import static org.junit.jupiter.api.Assertions.*;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlixxPlaceholderBuilderTest {
    @Nested
    class LiteralPlaceholder {
        @Test
        void buildWithStaticValue() {
            BlixxPlaceholder<String> placeholder = BlixxPlaceholder.<String>builder()
                    .literal()
                    .withKey("test")
                    .withValue("hello")
                    .build();

            assertInstanceOf(BlixxPlaceholder.Literal.class, placeholder);
            assertEquals("hello", placeholder.get(null));
        }

        @Test
        void buildWithSupplierValue() {
            BlixxPlaceholder<String> placeholder = BlixxPlaceholder.<String>builder()
                    .literal()
                    .withKey("dynamic")
                    .withValue(() -> "computed")
                    .build();

            assertEquals("computed", placeholder.get(null));
        }

        @Test
        void keyIsAccessible() {
            BlixxPlaceholder<String> placeholder = BlixxPlaceholder.<String>builder()
                    .literal()
                    .withKey("mykey")
                    .withValue("val")
                    .build();

            BlixxPlaceholder.Literal<String> literal = (BlixxPlaceholder.Literal<String>) placeholder;
            assertEquals("mykey", literal.key());
        }

        @Test
        void possibleKeyAddedToKeys() {
            BlixxPlaceholder<String> placeholder = BlixxPlaceholder.<String>builder()
                    .literal()
                    .withKey("primary")
                    .withValue("val")
                    .withPossibleKey("alias")
                    .build();

            BlixxPlaceholder.Literal<String> literal = (BlixxPlaceholder.Literal<String>) placeholder;
            assertTrue(literal.keys().contains("primary"));
            assertTrue(literal.keys().contains("alias"));
        }

        @Test
        void staticFactoryEquivalent() {
            BlixxPlaceholder<String> fromFactory = BlixxPlaceholder.literal("key", "value");
            BlixxPlaceholder<String> fromBuilder = BlixxPlaceholder.<String>builder()
                    .literal()
                    .withKey("key")
                    .withValue("value")
                    .build();

            assertEquals(fromFactory.get(null), fromBuilder.get(null));
        }
    }

    @Nested
    class PatternPlaceholder {
        @Test
        void buildWithPattern() {
            Pattern pattern = Pattern.compile("color_(\\w+)");
            BlixxPlaceholder<String> placeholder = BlixxPlaceholder.<String>builder()
                    .pattern()
                    .withPattern(pattern)
                    .withMatcherSupplying(matcher -> "matched:" + matcher.group(1))
                    .build();

            assertInstanceOf(BlixxPlaceholder.Pattern.class, placeholder);
            assertEquals(pattern, ((BlixxPlaceholder.Pattern<String>) placeholder).pattern());
        }
    }

    @Nested
    class ContextualLiteralPlaceholder {
        @Test
        void buildWithContextSupplying() {
            BlixxPlaceholder<String> placeholder = BlixxPlaceholder.<String>builder()
                    .contextual()
                    .withExact(String.class)
                    .literal()
                    .withKey("ctx_placeholder")
                    .withContextSupplying(s -> "from:" + s)
                    .build();

            assertInstanceOf(BlixxPlaceholder.ContextualLiteral.class, placeholder);

            PlaceholderContext ctx = PlaceholderContext.create("world");
            assertEquals("from:world", placeholder.get(ctx));
        }

        @Test
        void contextualWithDefaultContext() {
            PlaceholderContext defaultCtx = PlaceholderContext.create("default");
            BlixxPlaceholder<String> placeholder = BlixxPlaceholder.<String>builder()
                    .contextual()
                    .withExact(String.class)
                    .literal()
                    .withKey("ctx_placeholder")
                    .withContextSupplying(s -> "from:" + s)
                    .withDefaultContext(defaultCtx)
                    .build();

            assertInstanceOf(BlixxPlaceholder.ContextualLiteral.class, placeholder);
        }
    }

    @Nested
    class ContextualPatternPlaceholder {
        @Test
        void buildWithContextAndMatcher() {
            Pattern pattern = Pattern.compile("data_(\\w+)");
            BlixxPlaceholder<String> placeholder = BlixxPlaceholder.<String>builder()
                    .contextual()
                    .withExact(String.class)
                    .pattern()
                    .withPattern(pattern)
                    .withContextAndMatcherSupplying((ctx, matcher) -> ctx + ":" + matcher.group(1))
                    .build();

            assertInstanceOf(BlixxPlaceholder.ContextualPattern.class, placeholder);
        }
    }
}
