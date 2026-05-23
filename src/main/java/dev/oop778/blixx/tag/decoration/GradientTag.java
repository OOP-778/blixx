package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.component.BlixxColor;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.BlixxTagHolder;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNode;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.StringQueue;
import java.util.*;
import java.util.regex.Matcher;
import lombok.*;
import org.jetbrains.annotations.NotNull;

public class GradientTag implements ColorChangingTag<GradientTag.GradientTagData> {
    public static final GradientTag INSTANCE = new GradientTag();
    public static final Processor PROCESSOR = new Processor();
    private static final java.util.regex.Pattern PARAMETER_VALUE_PATTERN =
            java.util.regex.Pattern.compile("([a-zA-Z]+)\\[([^]]+)");

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    @Override
    public GradientTagData createData(
            @NonNull BlixxProcessor.@NonNull ParserContext context, @NotNull StringQueue args) {
        final List<BlixxColor> colors = new ArrayList<>(args.size());
        final EnumMap<Parameter, Object> parameters = new EnumMap<>(Parameter.class);

        while (args.hasNext()) {
            final String pop = args.pop();
            final BlixxColor decode = ColorTag.decode(pop);
            if (decode == null) {
                try {
                    parameters.put(Parameter.PHASE, Float.parseFloat(pop));
                    continue;
                } catch (NumberFormatException ignored) {
                }
            }

            final Matcher matcher = PARAMETER_VALUE_PATTERN.matcher(pop);
            if (matcher.find()) {
                final String key = matcher.group(1);
                final String rawValue = matcher.group(2);

                final Parameter parameter = Parameter.valueOf(key.toUpperCase(Locale.ROOT));
                final Object o = parameter.parseValue(rawValue);
                if (o != null) {
                    parameters.put(parameter, o);
                }

                continue;
            }

            if (decode == null) {
                throw new IllegalStateException(String.format("Failed to parse color %s", pop));
            }

            colors.add(decode);
        }

        return new GradientTagData(colors.toArray(new BlixxColor[0]), parameters);
    }

    @Override
    public boolean canCoexist(@NonNull BlixxTag<?> other) {
        return !other.isInstanceOf(ColorChangingTag.class) && !(other.isInstanceOf(DecorationTag.class));
    }

    @RequiredArgsConstructor
    protected enum Parameter {
        PHASE(Float::parseFloat),
        EVERY(Integer::parseInt);
        private final CheckedFunction<String, Object> parser;

        public Object parseValue(String input) {
            try {
                return this.parser.apply(input);
            } catch (Throwable e) {
                throw new IllegalStateException(
                        String.format(
                                "Failed to parse parameter `%s` of input `%s`",
                                this.name().toLowerCase(Locale.ROOT), input),
                        e);
            }
        }
    }

    @FunctionalInterface
    protected interface CheckedFunction<INPUT, OUTPUT> {
        OUTPUT apply(INPUT input) throws Throwable;
    }

    @RequiredArgsConstructor
    @ToString
    @Getter
    public static class GradientTagData {
        private final BlixxColor[] colors;
        private final EnumMap<Parameter, Object> values;
        private TransitioningData transitioningData;

        public float getPhase() {
            return (float) this.values.getOrDefault(Parameter.PHASE, 0f);
        }

        public int getEvery() {
            return (int) this.values.getOrDefault(Parameter.EVERY, -1);
        }
    }

    @Data
    public static class TransitioningData {
        private final float position;
        private final float deltaPosition;
    }

    public static class Processor implements BlixxProcessor.Component.Visitor<GradientTagData> {
        @Override
        public void visit(@NonNull ComponentContext context) {
            final BlixxNode node = context.getNode();
            if (!(node instanceof BlixxTextNode)) {
                return;
            }

            final GradientTagData data = context.getData();
            final float phase = Math.max(-1f, Math.min(1f, data.getPhase()));

            final TransitioningData transitioningData =
                    this.getTransitioningData((BlixxNodeInternal) node, context.getTag());
            final int totalLength = this.calculateTotalLength((BlixxNodeInternal) node, context.getTag());

            float position = transitioningData == null ? 0f : transitioningData.position;
            if (position == 0) {
                position += phase;
            }

            final String nodeContent = ((BlixxTextNode) node).getContent();
            if (nodeContent.isEmpty() || data.getColors().length == 0) {
                return;
            }

            final BlixxColor[] colors = data.getColors();
            final int colorCount = colors.length;
            final int resetGradientEveryChars = data.getEvery();

            float deltaPosition = transitioningData == null
                    ? 1.0f / (resetGradientEveryChars == -1 ? totalLength + 1 : resetGradientEveryChars - 1)
                    : transitioningData.deltaPosition;

            // Adjust for small moves
            if (deltaPosition == 1f) {
                deltaPosition -= 0.1f;
            }

            final int maxColorIndex = colorCount - 1;

            BlixxColor lastInterpolatedColor = null;

            int charactersSinceReset = 0;
            for (int i = 0; i < nodeContent.length(); i++) {
                final char currentChar = nodeContent.charAt(i);

                if (Character.isWhitespace(currentChar)) {
                    context.appendGradientCharacter(currentChar, null);
                    continue;
                }

                // Reset gradient logic
                if (resetGradientEveryChars > 0 && charactersSinceReset == resetGradientEveryChars) {
                    position = 0f + phase;
                    charactersSinceReset = 0;
                }

                final float adjustedPosition = (position * maxColorIndex) % colorCount;
                final int startColorIndex = (int) adjustedPosition;
                final int endColorIndex = Math.min(startColorIndex + 1, maxColorIndex);

                final float interpolation = adjustedPosition - startColorIndex;

                if (endColorIndex >= colors.length) {
                    throw new IllegalStateException(String.format("Failed to append gradient to %s", nodeContent));
                }

                final BlixxColor interpolatedColor =
                        this.interpolateColor(colors[startColorIndex], colors[endColorIndex], interpolation);

                context.appendGradientCharacter(currentChar, interpolatedColor);
                lastInterpolatedColor = interpolatedColor;

                position += deltaPosition;
                charactersSinceReset++;
            }

            context.setContent("");
            data.transitioningData =
                    transitioningData == null ? new TransitioningData(position, deltaPosition) : transitioningData;
        }

        protected TransitioningData getTransitioningData(BlixxNodeInternal from, BlixxTag<?> tag) {
            final BlixxNodeInternal previousNode = from.getPrevious();
            if (previousNode == null) {
                return null;
            }

            if (!(previousNode instanceof BlixxTagHolder)) {
                return null;
            }

            final WithDefinedData<?> previousTag = ((BlixxTagHolder) previousNode).findTag(tag::equals);
            if (previousTag == null) {
                return null;
            }

            final GradientTagData definedData = (GradientTagData) previousTag.getDefinedData();
            return definedData.getTransitioningData();
        }

        protected BlixxColor interpolateColor(BlixxColor startColor, BlixxColor endColor, float factor) {
            final int startRed = startColor.red();
            final int startGreen = startColor.green();
            final int startBlue = startColor.blue();

            final int diffRed = endColor.red() - startRed;
            final int diffGreen = endColor.green() - startGreen;
            final int diffBlue = endColor.blue() - startBlue;

            final int interpolatedRed = startRed + (int) ((diffRed * factor) + 0.5f);
            final int interpolatedGreen = startGreen + (int) ((diffGreen * factor) + 0.5f);
            final int interpolatedBlue = startBlue + (int) ((diffBlue * factor) + 0.5f);

            return BlixxColor.of(interpolatedRed, interpolatedGreen, interpolatedBlue);
        }

        private int calculateTotalLength(BlixxNodeInternal from, WithDefinedData<?> tag) {
            final Iterator<BlixxNodeInternal> iterator = from.iterator(true);
            int nonSpaceCount = 0;

            while (iterator.hasNext()) {
                final BlixxNodeInternal next = iterator.next();
                if (!(next instanceof BlixxTextNode)) {
                    continue;
                }

                if (!((BlixxTextNode) next).hasTag(tag)) {
                    return nonSpaceCount;
                }

                for (final char c : ((BlixxTextNode) next).getContent().toCharArray()) {
                    if (Character.isWhitespace(c)) {
                        continue;
                    }

                    nonSpaceCount++;
                }
            }

            return nonSpaceCount;
        }
    }
}
