package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import dev.oop778.blixx.util.adventure.FastComponentBuilder;
import lombok.Data;
import lombok.NonNull;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GradientTag implements ColorChangingTag<GradientTag.GradientTagData> {
    public static final GradientTag INSTANCE = new GradientTag();
    public static final Processor PROCESSOR = new Processor();

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    @Override
    public GradientTagData createData(@NonNull BlixxProcessor.@NonNull ParserContext context, @NotNull BaseArgumentQueue args) {
        final List<TextColor> colors = new ArrayList<>(args.size());
        float phase = 0;

        while (args.hasNext()) {
            final String pop = args.pop();
            final TextColor decode = ColorTag.decode(pop);
            if (decode == null) {
                try {
                    phase = Float.parseFloat(pop);
                } catch (NumberFormatException ignored) {
                }
            }

            if (decode == null) {
                throw new IllegalStateException(String.format("Failed to parse color %s", pop));
            }

            colors.add(decode);
        }

        return new GradientTagData(colors.toArray(new TextColor[0]), phase);
    }

    @Override
    public boolean canCoexist(@NonNull BlixxProcessor.Context context, @NonNull BlixxTag<?> other) {
        return !other.isInstanceOf(ColorChangingTag.class);
    }

    @Data
    public static class GradientTagData {
        private final TextColor[] colors;
        private final float phase;
    }

    public static class Processor implements BlixxProcessor.Component.Visitor<GradientTagData> {
        @Override
        public void visit(@NonNull ComponentContext context) {
            final GradientTagData data = context.getData();
            final StringBuilder contentBefore = this.buildContentBefore((BlixxNodeImpl) context.getNode(), context.getTag());

            final String content = context.getNode().getContent();
            if (content.isEmpty() || data.getColors().length == 0) {
                return;
            }

            final int[] chars = content.chars()
                    .filter(codePoint -> !Character.isWhitespace(codePoint))
                    .toArray();

            final TextColor[] colors = data.getColors();
            final float phase = data.getPhase();
            final int colorCount = colors.length;
            final int length = chars.length;

            final int totalCharacters = Math.max(1, length - 1);
            final float deltaPosition = 1.0f / totalCharacters;

            float position = (contentBefore.length() + phase) / Math.max(1, totalCharacters + contentBefore.length());
            final int maxColorIndex = colorCount - 1;

            TextColor lastInterpolatedColor = null;
            Style lastStyle = null;

            final String nodeContent = context.getNode().getContent();
            final FastComponentBuilder fastComponentBuilder = context.getComponentBuilder();

            for (int i = 0; i < nodeContent.length(); i++) {
                final char currentChar = nodeContent.charAt(i);

                if (Character.isWhitespace(currentChar)) {
                    fastComponentBuilder.append(net.kyori.adventure.text.Component.text(currentChar));
                    continue;
                }

                final float adjustedPosition = position * maxColorIndex;
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
            }

            fastComponentBuilder.setContent("");  // Clear the content for the current node
        }

        protected StringBuilder buildContentBefore(BlixxNodeImpl from, BlixxTag<?> tag) {
            final StringBuilder builder = new StringBuilder();

            BlixxNodeImpl current = from.getPrevious();
            while (current != null && current.hasTag(tag::compare)) {
                for (final char c : current.getContent().toCharArray()) {
                    if (!Character.isWhitespace(c)) {
                        builder.append(c);
                        break;
                    }
                }

                current = current.getPrevious();
            }

            return builder;
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
