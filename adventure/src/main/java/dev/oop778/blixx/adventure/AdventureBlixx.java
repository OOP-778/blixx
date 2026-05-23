package dev.oop778.blixx.adventure;

import dev.oop778.blixx.adventure.util.FastComponentBuilder;
import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.BlixxStyle;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNode;
import dev.oop778.blixx.api.platform.BlixxPlatform;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

public class AdventureBlixx implements BlixxPlatform {
    public static final AdventureBlixx INSTANCE = new AdventureBlixx();

    @Override
    public @Nullable Object prebuild(Blixx blixx, BlixxTextNode node) {
        final BlixxProcessor.Component.ComponentContext context = new BlixxProcessor.Component.ComponentContext(blixx);
        final BlixxStyle blixxStyle = new BlixxStyle();

        context.setContent(node.getContent());
        context.setStyle(blixxStyle);

        for (final BlixxTag.WithDefinedData<?> tag : node.getTags()) {
            final BlixxProcessor processor = tag.getProcessor();
            context.setData(tag.getDefinedData());

            if (processor instanceof BlixxProcessor.Component.Decorator) {
                ((BlixxProcessor.Component.Decorator) processor).decorate(context);
            }
        }

        final Style style = AdventureStyleConverter.convert(blixxStyle, null);
        final FastComponentBuilder componentBuilder = new FastComponentBuilder();
        componentBuilder.setContent(context.getContent());
        componentBuilder.setStyle(style);

        return componentBuilder.build();
    }

    @Override
    public Component build(BlixxComponent root) {
        return AdventureNodeBuilder.build((BlixxNodeInternal) root.getNode(), null);
    }
}
