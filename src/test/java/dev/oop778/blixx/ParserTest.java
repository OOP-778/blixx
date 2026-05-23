package dev.oop778.blixx;

import static org.junit.jupiter.api.Assertions.*;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.BlixxTagHolder;
import dev.oop778.blixx.api.parser.node.kind.BlixxPlaceholderNode;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNode;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.tag.BlixxTags;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ParserTest {
    private static Blixx blixx;

    @BeforeAll
    static void setup() {
        blixx = Blixx.builder()
                .withStandardParserConfig(configurator -> configurator
                        .withTags(BlixxTags.STANDARD)
                        .withPlaceholderFormat('%', '%')
                        .withPlaceholderFormat('{', '}'))
                .withStandardPlaceholderConfig()
                .build();
    }

    private List<BlixxNodeInternal> collectNodes(BlixxNodeInternal root) {
        List<BlixxNodeInternal> nodes = new ArrayList<>();
        Iterator<BlixxNodeInternal> it = root.iterator(true);
        while (it.hasNext()) {
            nodes.add(it.next());
        }
        return nodes;
    }

    @Nested
    class PlainText {
        @Test
        void parsesSimpleText() {
            BlixxNodeInternal node = (BlixxNodeInternal) blixx.parseNode("Hello World");
            assertInstanceOf(BlixxTextNode.class, node);
            assertEquals("Hello World", ((BlixxTextNode) node).getContent());
        }

        @Test
        void parsesEmptyString() {
            BlixxNodeInternal node = (BlixxNodeInternal) blixx.parseNode("");
            assertInstanceOf(BlixxTextNode.class, node);
            assertEquals("", ((BlixxTextNode) node).getContent());
        }

        @Test
        void preservesWhitespace() {
            BlixxNodeInternal node = (BlixxNodeInternal) blixx.parseNode("  hello  world  ");
            assertInstanceOf(BlixxTextNode.class, node);
            assertEquals("  hello  world  ", ((BlixxTextNode) node).getContent());
        }

        @Test
        void preservesNewlines() {
            BlixxNodeInternal node = (BlixxNodeInternal) blixx.parseNode("line1\nline2");
            assertInstanceOf(BlixxTextNode.class, node);
            assertEquals("line1\nline2", ((BlixxTextNode) node).getContent());
        }
    }

    @Nested
    class TagParsing {
        @Test
        void parsesSingleColorTag() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<red>Hello");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            boolean foundText = false;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxTextNode) {
                    BlixxTextNode textNode = (BlixxTextNode) node;
                    if ("Hello".equals(textNode.getContent())) {
                        foundText = true;
                        assertTrue(textNode.hasTags());
                    }
                }
            }
            assertTrue(foundText);
        }

        @Test
        void parsesNestedTags() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<red><bold>Hello</bold></red>");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            boolean foundHello = false;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxTextNode && "Hello".equals(((BlixxTextNode) node).getContent())) {
                    foundHello = true;
                    assertTrue(((BlixxTextNode) node).hasTags());
                }
            }
            assertTrue(foundHello);
        }

        @Test
        void parsesClosingTag() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<bold>Hello</bold> World");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            boolean foundWorld = false;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxTextNode && " World".equals(((BlixxTextNode) node).getContent())) {
                    foundWorld = true;
                    assertFalse(((BlixxTagHolder) node).hasTags());
                }
            }
            assertTrue(foundWorld);
        }

        @Test
        void parsesNamedColorTags() {
            String[] colors = {"red", "blue", "green", "yellow", "gold", "gray", "white", "black"};
            for (String color : colors) {
                BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<" + color + ">test");
                List<BlixxNodeInternal> nodes = collectNodes(root);

                boolean foundTaggedText = false;
                for (BlixxNodeInternal node : nodes) {
                    if (node instanceof BlixxTextNode && "test".equals(((BlixxTextNode) node).getContent())) {
                        foundTaggedText = true;
                        assertTrue(((BlixxTextNode) node).hasTags());
                    }
                }
                assertTrue(foundTaggedText, "Color tag <" + color + "> should produce tagged text node");
            }
        }

        @Test
        void parsesDecorationTags() {
            String[] decorations = {"bold", "italic", "underlined", "strikethrough", "obfuscated"};
            for (String deco : decorations) {
                BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<" + deco + ">test");
                List<BlixxNodeInternal> nodes = collectNodes(root);

                boolean found = false;
                for (BlixxNodeInternal node : nodes) {
                    if (node instanceof BlixxTextNode && "test".equals(((BlixxTextNode) node).getContent())) {
                        found = true;
                        assertTrue(((BlixxTextNode) node).hasTags());
                    }
                }
                assertTrue(found, "Decoration <" + deco + "> should produce tagged text");
            }
        }

        @Test
        void parsesDecorationAliases() {
            String[] aliases = {"b", "i", "em", "u", "st", "obf"};
            for (String alias : aliases) {
                BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<" + alias + ">test");
                List<BlixxNodeInternal> nodes = collectNodes(root);

                boolean found = false;
                for (BlixxNodeInternal node : nodes) {
                    if (node instanceof BlixxTextNode && "test".equals(((BlixxTextNode) node).getContent())) {
                        found = true;
                        assertTrue(((BlixxTextNode) node).hasTags());
                    }
                }
                assertTrue(found, "Alias <" + alias + "> should produce tagged text");
            }
        }

        @Test
        void parsesResetTag() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<red><bold>Hello<reset>World");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            boolean foundWorld = false;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxTextNode && "World".equals(((BlixxTextNode) node).getContent())) {
                    foundWorld = true;
                    assertFalse(((BlixxTextNode) node).hasTags());
                }
            }
            assertTrue(foundWorld);
        }

        @Test
        void unknownTagTreatedAsPlainText() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<unknowntag>Hello");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            boolean foundContent = false;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxTextNode) {
                    String content = ((BlixxTextNode) node).getContent();
                    if (content.contains("unknowntag") || content.contains("Hello")) {
                        foundContent = true;
                    }
                }
            }
            assertTrue(foundContent);
        }
    }

    @Nested
    class EscapeHandling {
        @Test
        void backslashEscapesTagOpen() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("x\\<red>Hello");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            boolean foundUntagged = false;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxTextNode) {
                    String content = ((BlixxTextNode) node).getContent();
                    if (content.contains("<red>")) {
                        foundUntagged = true;
                    }
                }
            }
            assertTrue(foundUntagged);
        }
    }

    @Nested
    class PlaceholderParsing {
        @Test
        void parsesCurlyBracePlaceholder() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("Hello {player}");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            boolean foundPlaceholder = false;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxPlaceholderNode) {
                    foundPlaceholder = true;
                    assertEquals("player", ((BlixxPlaceholderNode) node).getPlaceholder());
                }
            }
            assertTrue(foundPlaceholder);
        }

        @Test
        void parsesMultiplePlaceholders() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("{greeting} {name}!");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            int placeholderCount = 0;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxPlaceholderNode) {
                    placeholderCount++;
                }
            }
            assertEquals(2, placeholderCount);
        }

        @Test
        void placeholderWithinTaggedText() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<red>Hello {player}");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            boolean foundPlaceholder = false;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxPlaceholderNode) {
                    foundPlaceholder = true;
                    BlixxPlaceholderNode pNode = (BlixxPlaceholderNode) node;
                    assertEquals("player", pNode.getPlaceholder());
                    assertTrue(pNode.hasTags());
                }
            }
            assertTrue(foundPlaceholder);
        }

        @Test
        void sameCharDelimiterTreatedAsText() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("Hello %player%");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            boolean hasPlaceholderNode = false;
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxPlaceholderNode) {
                    hasPlaceholderNode = true;
                }
            }
            assertFalse(hasPlaceholderNode, "%..% same-char delimiters don't create placeholder nodes at parse time");
        }
    }

    @Nested
    class NodeStructure {
        @Test
        void singleTextNodeHasNoNextOrPrevious() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("Hello");
            assertNull(root.getNext());
            assertNull(root.getPrevious());
        }

        @Test
        void multipleNodesFormLinkedList() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<red>Hello<blue>World");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            assertTrue(nodes.size() >= 2);

            assertNull(nodes.get(0).getPrevious());
            for (int i = 1; i < nodes.size(); i++) {
                assertNotNull(nodes.get(i).getPrevious());
            }
            assertNull(nodes.get(nodes.size() - 1).getNext());
        }

        @Test
        void findTreeEndReturnsLastNode() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<red>A<blue>B<green>C");
            BlixxNodeInternal end = root.findTreeEnd();

            assertNull(end.getNext());
            if (end instanceof BlixxTextNode) {
                assertEquals("C", ((BlixxTextNode) end).getContent());
            }
        }

        @Test
        void copyCreatesIndependentTree() {
            BlixxNodeInternal root = (BlixxNodeInternal) blixx.parseNode("<red>Hello<blue>World");
            BlixxNodeInternal copy = root.copy();

            assertNotSame(root, copy);

            List<BlixxNodeInternal> origNodes = collectNodes(root);
            List<BlixxNodeInternal> copyNodes = collectNodes(copy);
            assertEquals(origNodes.size(), copyNodes.size());

            for (int i = 0; i < origNodes.size(); i++) {
                assertNotSame(origNodes.get(i), copyNodes.get(i));
            }
        }
    }

    @Nested
    class ComponentIntegration {
        @Test
        void parseComponentWrapsNode() {
            BlixxComponent component = blixx.parseComponent("Hello");
            assertNotNull(component);
            assertNotNull(component.getNode());
        }

        @Test
        void componentCopyIsIndependent() {
            BlixxComponent original = blixx.parseComponent("Hello");
            BlixxComponent copy = original.copy();

            assertNotSame(original, copy);
            assertNotSame(original.getNode(), copy.getNode());
        }

        @Test
        void componentAppendLinksNodes() {
            BlixxComponent a = blixx.parseComponent("Hello");
            BlixxComponent b = blixx.parseComponent(" World");
            BlixxComponent result = a.append(b);

            assertNotSame(a, result);

            BlixxNodeInternal node = (BlixxNodeInternal) result.getNode();
            assertNotNull(node.findTreeEnd());

            List<BlixxNodeInternal> nodes = collectNodes(node);
            assertTrue(nodes.size() >= 2);
        }

        @Test
        void componentAppendMultiple() {
            BlixxComponent a = blixx.parseComponent("A");
            BlixxComponent b = blixx.parseComponent("B");
            BlixxComponent c = blixx.parseComponent("C");
            BlixxComponent result = a.append(b, c);

            BlixxNodeInternal node = (BlixxNodeInternal) result.getNode();
            List<BlixxNodeInternal> nodes = collectNodes(node);
            assertTrue(nodes.size() >= 3);
        }

        @Test
        void emptyComponentHasEmptyContent() {
            BlixxComponent empty = BlixxComponent.empty();
            BlixxNodeInternal node = (BlixxNodeInternal) empty.getNode();
            assertInstanceOf(BlixxTextNode.class, node);
            assertEquals("", ((BlixxTextNode) node).getContent());
        }

        @Test
        void spaceComponentHasSpace() {
            BlixxComponent space = BlixxComponent.space();
            BlixxNodeInternal node = (BlixxNodeInternal) space.getNode();
            assertInstanceOf(BlixxTextNode.class, node);
            assertEquals(" ", ((BlixxTextNode) node).getContent());
        }

        @Test
        void newLineComponentHasNewline() {
            BlixxComponent nl = BlixxComponent.newLine();
            BlixxNodeInternal node = (BlixxNodeInternal) nl.getNode();
            assertInstanceOf(BlixxTextNode.class, node);
            assertEquals("\n", ((BlixxTextNode) node).getContent());
        }

        @Test
        void joinCombinesWithDelimiter() {
            List<BlixxComponent> components = new ArrayList<>();
            components.add(blixx.parseComponent("A"));
            components.add(blixx.parseComponent("B"));
            components.add(blixx.parseComponent("C"));

            BlixxComponent result = BlixxComponent.join(components, BlixxComponent.space());
            assertNotNull(result);

            BlixxNodeInternal node = (BlixxNodeInternal) result.getNode();
            List<BlixxNodeInternal> nodes = collectNodes(node);
            assertTrue(nodes.size() >= 5);
        }

        @Test
        void joinEmptyListReturnsEmptyComponent() {
            List<BlixxComponent> empty = new ArrayList<>();
            BlixxComponent result = BlixxComponent.join(empty, BlixxComponent.space());

            BlixxNodeInternal node = (BlixxNodeInternal) result.getNode();
            assertInstanceOf(BlixxTextNode.class, node);
            assertEquals("", ((BlixxTextNode) node).getContent());
        }
    }

    @Nested
    class ParsePlaceholders {
        @Test
        void parsePlaceholderReplacesAtParseTime() {
            Blixx blixxWithParsePlaceholder = Blixx.builder()
                    .withStandardParserConfig(configurator -> configurator
                            .withTags(BlixxTags.STANDARD)
                            .withPlaceholderFormat('%', '%')
                            .withParsePlaceholder(BlixxPlaceholder.<String>builder()
                                    .literal()
                                    .withKey("server_name")
                                    .withValue(() -> "MyServer")
                                    .build()))
                    .withStandardPlaceholderConfig()
                    .build();

            BlixxNodeInternal root =
                    (BlixxNodeInternal) blixxWithParsePlaceholder.parseNode("Welcome to %server_name%!");
            List<BlixxNodeInternal> nodes = collectNodes(root);

            StringBuilder fullText = new StringBuilder();
            for (BlixxNodeInternal node : nodes) {
                if (node instanceof BlixxTextNode) {
                    fullText.append(((BlixxTextNode) node).getContent());
                }
            }
            assertTrue(fullText.toString().contains("MyServer"));
        }
    }

    @Nested
    class BlixxStandard {
        @Test
        void standardInstanceWorks() {
            Blixx standard = Blixx.standard();
            assertNotNull(standard);
            assertNotNull(standard.parserConfig());
            assertNotNull(standard.placeholderConfig());
        }

        @Test
        void standardInstanceIsSingleton() {
            assertSame(Blixx.standard(), Blixx.standard());
        }
    }
}
