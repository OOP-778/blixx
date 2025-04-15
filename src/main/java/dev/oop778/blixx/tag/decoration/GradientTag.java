package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import dev.oop778.blixx.util.adventure.FastComponentBuilder;
import lombok.*;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

public class GradientTag implements ColorChangingTag<GradientTag.GradientTagData> {
    public static final GradientTag INSTANCE = new GradientTag();
    public static final Processor PROCESSOR = new Processor();
    private static final java.util.regex.Pattern PARAMETER_VALUE_PATTERN = java.util.regex.Pattern.compile(
            "([a-zA-Z]+)\\[([^]]+)");

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    @Override
    public GradientTagData createData(@NonNull BlixxProcessor.@NonNull ParserContext context, @NotNull BaseArgumentQueue args) {
        final List<TextColor> colors = new ArrayList<>(args.size());
        final EnumMap<Parameter, Object> parameters = new EnumMap<>(Parameter.class);

        while (args.hasNext()) {
            final String pop = args.pop();
            final TextColor decode = ColorTag.decode(pop);
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

        return new GradientTagData(colors.toArray(new TextColor[0]), parameters);
    }

    @Override
    public boolean canCoexist(@NonNull BlixxTag<?> other) {
        return !other.isInstanceOf(ColorChangingTag.class);
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
                throw new IllegalStateException(String.format("Failed to parse parameter `%s` of input `%s`", this.name().toLowerCase(Locale.ROOT), input), e);
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
        private final TextColor[] colors;
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
            final GradientTagData data = context.getData();
            final float phase = Math.max(-1f, Math.min(1f, data.getPhase()));

            final TransitioningData transitioningData = this.getTransitioningData((BlixxNodeImpl) context.getNode(), context.getTag());

            float position = transitioningData == null ? 0f : transitioningData.position;
            if (position == 0) {
                position += phase;
            }

            final String content = context.getNode().getContent();
            if (content.isEmpty() || data.getColors().length == 0) {
                return;
            }

            final int[] chars = content.chars()
                    .filter(codePoint -> !Character.isWhitespace(codePoint))
                    .toArray();

            final TextColor[] colors = data.getColors();
            final int colorCount = colors.length;
            final int resetGradientEveryChars = data.getEvery();

            final int ourCharacterCount = Math.max(1, chars.length - 1);
            float deltaPosition = transitioningData == null ? 1.0f / (resetGradientEveryChars == -1 ? ourCharacterCount : resetGradientEveryChars - 1) : transitioningData.deltaPosition;

            // Adjust for small moves
            if (deltaPosition == 1f) {
                deltaPosition -= 0.1f;
            }

            final int maxColorIndex = colorCount - 1;

            TextColor lastInterpolatedColor = null;
            Style lastStyle = null;

            final String nodeContent = context.getNode().getContent();
            final FastComponentBuilder fastComponentBuilder = context.getComponentBuilder();

            int charactersSinceReset = 0;
            for (int i = 0; i < nodeContent.length(); i++) {
                final char currentChar = nodeContent.charAt(i);

                if (Character.isWhitespace(currentChar)) {
                    fastComponentBuilder.append(net.kyori.adventure.text.Component.text(currentChar));
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
                    throw new IllegalStateException(String.format("Failed to append gradient to %s", content));
                }

                final TextColor interpolatedColor = this.interpolateColor(colors[startColorIndex], colors[endColorIndex], interpolation);

                if (interpolatedColor.equals(lastInterpolatedColor)) {
                    fastComponentBuilder.append(net.kyori.adventure.text.Component.text(currentChar, lastStyle));
                } else {
                    lastInterpolatedColor = interpolatedColor;
                    lastStyle = Style.style(interpolatedColor);
                    fastComponentBuilder.append(net.kyori.adventure.text.Component.text(currentChar, lastStyle));
                }

                position += deltaPosition;
                charactersSinceReset++;
            }

            fastComponentBuilder.setContent("");
            data.transitioningData = transitioningData == null ? new TransitioningData(position, deltaPosition) : transitioningData;
        }

        protected TransitioningData getTransitioningData(BlixxNodeImpl from, BlixxTag<?> tag) {
            final BlixxNodeImpl previousNode = from.getPrevious();
            if (previousNode == null) {
                return null;
            }

            final WithDefinedData<?> previousTag = previousNode.findTag(tag::equals);
            if (previousTag == null) {
                return null;
            }

            final GradientTagData definedData = (GradientTagData) previousTag.getDefinedData();
            return definedData.getTransitioningData();
        }

        protected TextColor interpolateColor(TextColor startColor, TextColor endColor, float factor) {
            final int startRed = startColor.red();
            final int startGreen = startColor.green();
            final int startBlue = startColor.blue();

            final int diffRed = endColor.red() - startRed;
            final int diffGreen = endColor.green() - startGreen;
            final int diffBlue = endColor.blue() - startBlue;

            final int interpolatedRed = startRed + (int) ((diffRed * factor) + 0.5f);
            final int interpolatedGreen = startGreen + (int) ((diffGreen * factor) + 0.5f);
            final int interpolatedBlue = startBlue + (int) ((diffBlue * factor) + 0.5f);

            return TextColor.color(interpolatedRed, interpolatedGreen, interpolatedBlue);
        }
    }
}
