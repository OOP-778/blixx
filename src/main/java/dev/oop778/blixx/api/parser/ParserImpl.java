package dev.oop778.blixx.api.parser;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.config.ParserConfig;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.kind.BlixxPlaceholderCloseNode;
import dev.oop778.blixx.api.parser.node.kind.BlixxPlaceholderNode;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNode;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNodeImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.support.LegacyFormatSupport;
import dev.oop778.blixx.util.StringQueue;
import dev.oop778.blixx.util.collection.ArrayCharacterQueue;
import dev.oop778.blixx.util.collection.ObjectArray;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public class ParserImpl {
    private final Blixx blixx;
    private final ParserConfig parserConfig;
    private final PlaceholderContext context;

    public ParserImpl(Blixx blixx) {
        this.blixx = blixx;
        this.parserConfig = blixx.parserConfig();
        this.context = blixx.placeholderConfig().defaultContext();
    }

    public BlixxNodeInternal parse(String text, PlaceholderContext context) {
        final String preprocessInput = this.preprocessInput(text, PlaceholderContext.compose(this.context, context));
        final ParsingContext parsingContext = new ParsingContext(preprocessInput, text);
        parsingContext.parse();

        this.postParse(parsingContext.rootNode);
        this.assignBlixx(parsingContext.rootNode);

        return parsingContext.rootNode;
    }

    private void assignBlixx(BlixxNodeInternal node) {
        final Iterator<BlixxNodeInternal> iterator = node.iterator(true);
        while (iterator.hasNext()) {
            iterator.next().setBlixx(this.blixx);
        }
    }

    private String preprocessInput(String input, PlaceholderContext context) {
        if (this.parserConfig.supportLegacyFormat()) {
            input = LegacyFormatSupport.preprocessInput(input);
        }

        for (final BlixxPlaceholder<String> parsePlaceholder : ParserImpl.this.parserConfig.parsePlaceholders()) {
            if (parsePlaceholder instanceof BlixxPlaceholder.Literal) {
                for (final String key : ((BlixxPlaceholder.Literal<?>) parsePlaceholder).keys()) {
                    input = !input.contains(key) ? input : input.replace(key, parsePlaceholder.get(context));
                }
            }

            if (parsePlaceholder instanceof BlixxPlaceholder.Pattern) {
                final Pattern pattern = ((BlixxPlaceholder.Pattern<?>) parsePlaceholder).pattern();
                final Matcher matcher = pattern.matcher(input);
                while (matcher.find()) {
                    final String replacement = parsePlaceholder.get(
                            PlaceholderContext.compose(PlaceholderContext.create(matcher), context));
                    input = input.replace(matcher.group(), replacement);
                }
            }
        }

        return input;
    }

    private void postParse(BlixxNodeInternal node) {
        final Iterator<BlixxNodeInternal> iterator = node.iterator(true);
        while (iterator.hasNext()) {
            final BlixxNodeInternal next = iterator.next();
            this.tryPreparse(next);
        }
    }

    private void tryPreparse(BlixxNodeInternal node) {
        if (!(node instanceof BlixxTextNode)) {
            return;
        }

        final BlixxTextNode textNode = (BlixxTextNode) node;

        // If has dynamic tags, we don't preparse it
        if (textNode.hasTag(tag -> tag.getProcessor() instanceof BlixxProcessor.Component.Visitor
                || tag.getDefinedData() instanceof BlixxPlaceholderNode)) {
            return;
        }

        if (this.blixx.platform() != null) {
            final Object prebuilt = this.blixx.platform().prebuild(this.blixx, textNode);
            if (prebuilt != null) {
                textNode.setPreBuilt(prebuilt);
            }
        }
    }

    private class ParsingContext {
        private final ArrayCharacterQueue charQueue;
        private final StringBuilder builder = new StringBuilder();
        private final BlixxProcessor.ParserContext parserContext;
        private ObjectArray<BlixxTag.WithDefinedData<?>> tags;

        @Nullable
        private BlixxNodeInternal rootNode;

        @Nullable
        private BlixxNodeInternal currentNode;

        private BlixxTag.WithDefinedData<?> lastParsedTag;

        public ParsingContext(String input, String originalInput) {
            this.charQueue = new ArrayCharacterQueue(input);
            this.tags = new ObjectArray<>(1);
            this.parserContext = BlixxProcessor.ParserContext.builder()
                    .blixx(ParserImpl.this.blixx)
                    .build();
        }

        public void parse() {
            while (this.charQueue.hasNext()) {
                final char next = this.charQueue.next();

                // Try Parsing Tag
                if (this.tryFindNextTag()) {
                    continue;
                }

                // Try parsing placeholders
                if (this.tryFindPlaceholder()) {
                    continue;
                }

                // Do not include escapes in text
                if (next == '\\' && this.charQueue.hasNext()) {
                    continue;
                }

                this.builder.append(next);
            }

            this.createTextNode(null);
            if (this.rootNode == null) {
                this.rootNode = new BlixxTextNodeImpl("", this.tags);
            }
        }

        private BlixxPlaceholderNode createPlaceholderNode(String placeholder) {
            if (this.builder.length() > 0) {
                this.createTextNode(null);
            }

            final BlixxPlaceholderNode placeholderNode =
                    new BlixxPlaceholderNode(placeholder, TagCopier.copyTags(this.tags));
            this.updateCurrentNode(placeholderNode);

            return placeholderNode;
        }

        private void createTextNode(@Nullable Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
            if (this.builder.length() == 0) {
                return;
            }

            final String content = this.builder.toString();
            this.builder.setLength(0);

            final BlixxTextNodeImpl blixxTextNode = new BlixxTextNodeImpl(
                    content, tagFilterer == null ? this.tags.copy(null) : this.tags.filter(tagFilterer));
            this.updateCurrentNode(blixxTextNode);
        }

        private void updateCurrentNode(BlixxNodeInternal node) {
            if (this.rootNode == null) {
                this.rootNode = node;
            }

            node.setPrevious(this.currentNode);

            if (this.currentNode != null) {
                this.currentNode.setNext(node);
            }

            this.currentNode = node;
        }

        private boolean tryFindPlaceholder() {
            final boolean containsPlaceholderOpen =
                    ParserImpl.this.parserConfig.placeholderCharacters().contains(this.charQueue.current());
            if (!containsPlaceholderOpen || this.charQueue.isPreviousEscape()) {
                return false;
            }

            final int placeholderStart = this.charQueue.currentIndex();
            final int ending = this.charQueue.findEnding(
                    (endChar) ->
                            ParserImpl.this.parserConfig.placeholderCharacters().contains(endChar),
                    true,
                    true);
            if (ending == -1) {
                return false;
            }

            final String placeholder = this.charQueue.makeStringOfRange(placeholderStart + 1, ending - 1);

            // Closing placeholder
            if (placeholder.startsWith("/")) {
                this.createPlaceholderNodeClose(placeholder);
                return true;
            }

            this.createPlaceholderNode(placeholder);
            return true;
        }

        private void createPlaceholderNodeClose(String placeholder) {
            // find last node that contains this placeholder
            if (this.currentNode == null) {
                return;
            }

            placeholder = placeholder.substring(1);
            final Object key = new Object();

            // Find last tag that contains this placeholder and mark it
            BlixxNodeInternal currentNode = this.currentNode;
            while (currentNode != null) {
                if (currentNode instanceof BlixxPlaceholderNode
                        && ((BlixxPlaceholderNode) currentNode).getPlaceholder().equals(placeholder)) {
                    ((BlixxPlaceholderNode) currentNode).setClosingKey(key);
                    break;
                }

                currentNode = currentNode.getPrevious();
            }

            if (this.builder.length() > 0) {
                this.createTextNode(null);
            }

            final BlixxPlaceholderCloseNode placeholderNode = new BlixxPlaceholderCloseNode(key);
            this.updateCurrentNode(placeholderNode);
        }

        private boolean tryFindNextTag() {
            // Check if current car is tag opening and if it wasn't escaped
            if (this.charQueue.current() != ParserImpl.this.parserConfig.tagOpen()
                    || this.charQueue.isPreviousEscape()) {
                return false;
            }

            // Get the start of the tag
            final int parsingStart = this.charQueue.currentIndex();

            // Check if tag is closing
            final boolean isClosing = this.charQueue.hasNext() && this.charQueue.peek() == '/';

            // Check if we can find tag closing
            final int ending = this.charQueue.findEnding(ParserImpl.this.parserConfig.tagClose(), true, true);

            if (ending == -1) {
                return false;
            }

            // Get contents inside tag
            final String[] tagContent = this.charQueue.makeStringOfRangeSplitBy(parsingStart + 1, ending - 1, ':');
            if (isClosing) {
                final String closingTagName = tagContent[0].substring(1);
                this.tryCloseTag(closingTagName);
                return true;
            }

            final BlixxTag.WithDefinedData<?> parsedTag = this.tryParseTag(tagContent);
            if (parsedTag == null) {
                this.charQueue.jump(parsingStart);
                return false;
            }

            if (this.isTagAlreadyUsed(parsedTag) && !(this.currentNode instanceof BlixxPlaceholderNode)) {
                return true;
            }

            return this.processNewTag(parsedTag);
        }

        private void tryCloseTag(String closingTagName) {
            if (this.lastParsedTag == null) {
                return;
            }

            final BlixxTag<?> blixxTag =
                    ParserImpl.this.blixx.parserConfig().tags().get(closingTagName);
            if (blixxTag == null) {
                return;
            }

            this.createTextNode(null);
            this.tags = this.tags.filter(nodeTag -> !blixxTag.compare(nodeTag));
        }

        private boolean processNewTag(BlixxTag.WithDefinedData<?> parsedTag) {
            if (this.builder.length() != 0) {
                this.createTextNode(null);
                this.tags = this.tags.filter(parsedTag::canCoexist);
            }

            final BlixxProcessor.Context build = BlixxProcessor.Context.builder()
                    .blixx(ParserImpl.this.blixx)
                    .build();

            final BlixxProcessor processor = parsedTag.getProcessor();
            if (processor instanceof BlixxProcessor.Tree.Filterer) {
                this.tags = ((BlixxProcessor.Tree.Filterer) processor).filter(build, this.tags);
                return true;
            }

            this.tags.add(parsedTag);
            this.lastParsedTag = parsedTag;

            return true;
        }

        private boolean isTagAlreadyUsed(BlixxTag.WithDefinedData<?> parsedTag) {
            return (this.lastParsedTag != null && this.lastParsedTag.compareWithData(parsedTag))
                    || this.tags != null && this.tags.stream().anyMatch(parsedTag::compare);
        }

        private <T> BlixxTag.WithDefinedData<T> tryParseTag(String[] potentialTag) {
            final StringQueue baseArgumentQueue = new StringQueue(potentialTag);
            if (!baseArgumentQueue.hasNext()) {
                return null;
            }

            final String name = baseArgumentQueue.pop();
            final BlixxTag<T> tag =
                    (BlixxTag<T>) ParserImpl.this.blixx.parserConfig().tags().get(name);
            if (tag == null) {
                return this.tryParsePatternBasedTag(potentialTag[0]);
            }

            if (tag instanceof BlixxTag.NoData) {
                return new TagWithDefinedDataImpl<>(tag, null);
            }

            if (tag instanceof BlixxTag.WithDefinedData<?>) {
                return (BlixxTag.WithDefinedData<T>) tag;
            }

            final T data = tag.createData(parserContext, baseArgumentQueue);
            return new TagWithDefinedDataImpl<>(tag, data);
        }

        private <T> TagWithDefinedDataImpl<T> tryParsePatternBasedTag(String potentialTag) {
            for (final BlixxTag.Pattern<?> patternBasedTag :
                    ParserImpl.this.blixx.parserConfig().patternTags()) {
                final Pattern pattern = patternBasedTag.getPattern();
                final Matcher matcher = pattern.matcher(potentialTag);
                if (!matcher.find()) {
                    continue;
                }

                final T data = (T) patternBasedTag.createDataOfMatcher(parserContext, matcher);
                return new TagWithDefinedDataImpl<>(((BlixxTag.Pattern<T>) patternBasedTag), data);
            }

            return null;
        }
    }
}
