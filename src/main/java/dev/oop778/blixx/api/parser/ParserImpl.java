package dev.oop778.blixx.api.parser;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.config.ParserConfig;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import dev.oop778.blixx.util.ArrayCharacterQueue;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserImpl {
    private final Blixx blixx;
    private final ParserConfig parserConfig;
    private final PlaceholderContext context;

    public ParserImpl(Blixx blixx) {
        this.blixx = blixx;
        this.parserConfig = blixx.parserConfig();
        this.context = blixx.placeholderConfig().defaultContext().context();
    }

    public BlixxNodeImpl parse(String text, PlaceholderContext context) {
        final String preprocessInput = this.preprocessInput(text, PlaceholderContext.compose(this.context, context));
        final ParsingContext parsingContext = new ParsingContext(preprocessInput, text);
        parsingContext.parse();

        this.postParse(parsingContext.rootNode);

        return parsingContext.rootNode;
    }

    private String preprocessInput(String input, PlaceholderContext context) {
        for (final BlixxPlaceholder<String> parsePlaceholder : ParserImpl.this.parserConfig.parsePlaceholders()) {
            if (parsePlaceholder instanceof BlixxPlaceholder.Literal<String> literalPlaceholder) {
                for (final String key : literalPlaceholder.keys()) {
                    input = !input.contains(key) ? key : input.replace(key, literalPlaceholder.get(context));
                }
            }

            if (parsePlaceholder instanceof BlixxPlaceholder.Pattern<String> patternPlaceholder) {
                final Pattern pattern = patternPlaceholder.pattern();
                final Matcher matcher = pattern.matcher(input);
                while (matcher.find()) {
                    final String replacement = patternPlaceholder.get(PlaceholderContext.compose(PlaceholderContext.create(matcher), context));
                    input = input.replace(matcher.group(), replacement);
                }
            }
        }

        return input;
    }

    private void postParse(BlixxNodeImpl node) {
        final Iterator<BlixxNodeImpl> iterator = node.iterator(true);
        while (iterator.hasNext()) {
            final BlixxNodeImpl next = iterator.next();
            if (!next.hasPlaceholders()) {
                next.parseIntoAdventure(this.blixx);
            }
        }
    }

    private class ParsingContext {
        private final BlixxNodeImpl rootNode;
        private final ArrayCharacterQueue charQueue;
        private final StringBuilder builder = new StringBuilder();
        private final Object parserKey;
        private BlixxNodeImpl currentNode;
        private BlixxTag.WithDefinedData<?> lastParsedTag;
        private int nodeIndex;
        private final BlixxNodeSpec spec;

        public ParsingContext(String input, String originalInput) {
            this.charQueue = new ArrayCharacterQueue(input);
            this.spec = new BlixxNodeSpec(originalInput);
            this.parserKey = new Object();
            this.rootNode = new BlixxNodeImpl(new NodeKey(this.nodeIndex++, this.parserKey), this.spec);
            this.currentNode = this.rootNode;
        }

        public void parse() {
            while (this.charQueue.hasNext()) {
                final char next = this.charQueue.next();

                // Try Parsing Tag
                if (!this.tryFindNextTag()) {
                    this.builder.append(next);
                }
            }

            this.finishNode();
        }

        private boolean tryFindNextTag() {
            // Check if current car is tag opening and if it wasn't escaped
            if (this.charQueue.current() != ParserImpl.this.parserConfig.tagOpen() || this.charQueue.isPreviousEscape()) {
                return false;
            }

            // Get the start of the tag
            final int parsingStart = this.charQueue.currentIndex();

            // Check if tag is closing
            final boolean isClosing = this.charQueue.hasNext() && this.charQueue.peek() == '/';

            // Check if we can find tag closing
            final int ending = this.charQueue.findEnding(ParserImpl.this.parserConfig.tagClose(), true, true);

            if (ending == -1) {
                // TODO: Check for strict mode
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
            if (parsedTag == null || this.isTagAlreadyUsed(parsedTag)) {
                return true;
            }

            return this.processNewTag(parsedTag);
        }

        private void tryCloseTag(String closingTagName) {
            if (this.lastParsedTag == null) {
                return;
            }

            BlixxTag.WithDefinedData<?> toRemoveTag = null;
            final BlixxTag<?> blixxTag = ParserImpl.this.blixx.parserConfig().tags().get(closingTagName);
            if (blixxTag == null) {
                // TODO: Throw error on strict mode, cause closing a tag that is not open
                return;
            }

            for (final BlixxTag.WithDefinedData<?> tag : this.currentNode.getTags()) {
                if (tag.compare(blixxTag)) {
                    toRemoveTag = tag;
                    break;
                }
            }

            if (toRemoveTag == null) {
                return;
            }

            final BlixxTag.WithDefinedData<?> finalToRemoveTag = toRemoveTag;
            this.moveOntoNewNode(tag -> tag == finalToRemoveTag);
        }

        private boolean processNewTag(BlixxTag.WithDefinedData<?> parsedTag) {
            if (!this.builder.isEmpty()) {
                final BlixxProcessor.Context tagContext = BlixxProcessor.Context.builder().blixx(ParserImpl.this.blixx).build();
                this.moveOntoNewNode(tag -> parsedTag.canCoexist(tagContext, tag));
            }

            final BlixxProcessor.Context build = BlixxProcessor.Context.builder()
                    .blixx(ParserImpl.this.blixx)
                    .build();

            final BlixxProcessor processor = parsedTag.getProcessor();
            if (processor instanceof BlixxProcessor.Tree.Filterer) {
                ((BlixxProcessor.Tree.Filterer) processor).filter(build, this.currentNode.getTags());
                return true;
            }

            this.currentNode.addTag(parsedTag);
            this.lastParsedTag = parsedTag;

            return true;
        }

        private void moveOntoNewNode(@Nullable Predicate<BlixxTag<?>> tagFilterer) {
            this.finishNode();
            this.currentNode = this.currentNode.createNextNode(new NodeKey(this.nodeIndex++, this.parserKey), tagFilterer);
        }

        private boolean isTagAlreadyUsed(BlixxTag.WithDefinedData<?> parsedTag) {
            return (this.lastParsedTag != null && this.lastParsedTag.compareWithData(parsedTag)) || this.currentNode.hasTag(parsedTag);
        }

        private <T> BlixxTag.WithDefinedData<T> tryParseTag(String[] potentialTag) {
            final BaseArgumentQueue baseArgumentQueue = new BaseArgumentQueue(potentialTag);
            if (!baseArgumentQueue.hasNext()) {
                return null;
            }

            final String name = baseArgumentQueue.pop();
            final BlixxTag<T> tag = (BlixxTag<T>) ParserImpl.this.blixx.parserConfig().tags().get(name);
            if (tag == null) {
                return this.tryParsePatternBasedTag(potentialTag[0]);
            }

            if (tag instanceof BlixxTag.NoData) {
                return new TagData<>(tag, null);
            }

            if (tag instanceof BlixxTag.WithDefinedData<?>) {
                return (BlixxTag.WithDefinedData<T>) tag;
            }

            final BlixxProcessor.Context build = BlixxProcessor.Context.builder()
                    .blixx(ParserImpl.this.blixx)
                    .build();

            final T data = tag.createData(build, baseArgumentQueue);
            return new TagData<>(tag, data);
        }

        private <T> TagData<T> tryParsePatternBasedTag(String potentialTag) {
            final BlixxProcessor.Context build = BlixxProcessor.Context.builder()
                    .blixx(ParserImpl.this.blixx)
                    .build();

            for (final BlixxTag.Pattern<?> patternBasedTag : ParserImpl.this.blixx.parserConfig().patternTags()) {
                final Pattern pattern = patternBasedTag.getPattern();
                final Matcher matcher = pattern.matcher(potentialTag);
                if (!matcher.find()) {
                    continue;
                }

                final T data = (T) patternBasedTag.createDataOfMatcher(build, matcher);
                return new TagData<>(((BlixxTag.Pattern<T>) patternBasedTag), data);
            }

            return null;
        }

        private void finishNode() {
            final String content = this.builder.toString();
            this.builder.setLength(0);

            this.currentNode.setContent(content);
            this.spec.indexPlaceholdersOf(this.currentNode, ParserImpl.this.blixx);
        }
    }

    @Data
    private static class TagData<T> implements BlixxTag.WithDefinedData<T>, BlixxTag.Wrapping<T> {
        private final BlixxTag<T> originalTag;
        private final T definedData;
    }
}
