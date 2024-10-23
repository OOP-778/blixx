package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import lombok.Data;
import lombok.NonNull;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

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
    public GradientTagData createData(BlixxProcessor.@NonNull Context context, @NotNull BaseArgumentQueue args) {
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
            final String content = context.getComponentBuilder().content();
            if (content.isEmpty() || data.getColors().length == 0) {
                return;
            }

            final TextColor[] colors = data.getColors();
            final float phase = data.getPhase();
            final int colorCount = colors.length;

            int visibleCharIndex = 0; // Tracks non-space/non-newline characters for interpolation

            for (int i = 0; i < content.length(); i++) {
                final char currentChar = content.charAt(i);

                // Skip spaces and newline characters
                if (Character.isWhitespace(currentChar)) {
                    // Append the whitespace character without applying any color
                    context.getComponentBuilder().append(net.kyori.adventure.text.Component.text(currentChar));
                    continue;
                }

                // Perform interpolation only on non-whitespace characters
                final float position = (visibleCharIndex + phase) / (float) (content.length() - 1);
                final int startColorIndex = (int) (position * (colorCount - 1));
                final int endColorIndex = Math.min(startColorIndex + 1, colorCount - 1);

                final float interpolation = position * (colorCount - 1) - startColorIndex;
                final TextColor interpolatedColor = this.interpolateColor(colors[startColorIndex], colors[endColorIndex], interpolation);

                // Apply the color to the current character
                context.getComponentBuilder().append(net.kyori.adventure.text.Component.text(currentChar, Style.style(interpolatedColor)));

                // Increment only when a visible character is processed
                visibleCharIndex++;
            }

            context.getComponentBuilder().content("");  // Clear the original content in the builder
        }

        protected TextColor interpolateColor(TextColor startColor, TextColor endColor, float factor) {
            final int startRed = startColor.red();
            final int startGreen = startColor.green();
            final int startBlue = startColor.blue();

            final int endRed = endColor.red();
            final int endGreen = endColor.green();
            final int endBlue = endColor.blue();

            final int interpolatedRed = (int) (startRed + (endRed - startRed) * factor);
            final int interpolatedGreen = (int) (startGreen + (endGreen - startGreen) * factor);
            final int interpolatedBlue = (int) (startBlue + (endBlue - startBlue) * factor);

            return TextColor.color(interpolatedRed, interpolatedGreen, interpolatedBlue);
        }
    }
}
