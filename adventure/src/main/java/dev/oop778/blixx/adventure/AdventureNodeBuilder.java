package dev.oop778.blixx.adventure;

import dev.oop778.blixx.adventure.util.FastComponentBuilder;
import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxStyle;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.BlixxNodeWithPrebuilt;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNode;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import java.util.Iterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

public class AdventureNodeBuilder {
    public static Component build(BlixxNodeInternal root, @Nullable Style defaultStyle) {
        final Blixx blixx = root.blixx();
        final BlixxProcessor.Component.ComponentContext context = new BlixxProcessor.Component.ComponentContext(blixx);

        final FastComponentBuilder rootBuilder = new FastComponentBuilder();
        rootBuilder.setContent("");
        rootBuilder.setStyle(defaultStyle);

        final Iterator<BlixxNodeInternal> iterator = root.iterator(true);

        while (iterator.hasNext()) {
            BlixxNodeInternal node = iterator.next();

            if (!(node instanceof BlixxNodeWithPrebuilt)) {
                continue;
            }

            final Object preBuilt = ((BlixxNodeWithPrebuilt) node).getPreBuilt();
            if (preBuilt != null) {
                rootBuilder.append((Component) preBuilt);
                continue;
            }

            if (!(node instanceof BlixxTextNode)) {
                continue;
            }

            final BlixxTextNode textNode = (BlixxTextNode) node;
            context.setNode(node);

            final FastComponentBuilder componentBuilder = new FastComponentBuilder();
            final BlixxStyle blixxStyle = new BlixxStyle();

            context.setContent(textNode.getContent());
            context.setStyle(blixxStyle);

            // Set up gradient character appender for Adventure
            context.setGradientCharacterAppender((character, color) -> {
                if (color == null) {
                    componentBuilder.append(Component.text(character));
                } else {
                    componentBuilder.append(
                            Component.text(character, Style.style(AdventureStyleConverter.convertColor(color))));
                }
            });

            // Process visitors
            for (final BlixxTag.WithDefinedData<?> tag : textNode.getTags()) {
                context.setData(tag.getDefinedData());
                context.setTag(tag);

                final BlixxProcessor processor = tag.getProcessor();
                if (processor instanceof BlixxProcessor.Component.Decorator) {
                    ((BlixxProcessor.Component.Decorator) processor).decorate(context);
                }

                if (processor instanceof BlixxProcessor.Component.Visitor) {
                    ((BlixxProcessor.Component.Visitor) processor).visit(context);
                }
            }

            componentBuilder.setContent(context.getContent());
            componentBuilder.setStyle(AdventureStyleConverter.convert(blixxStyle, defaultStyle));
            rootBuilder.append(componentBuilder.build());
        }

        return rootBuilder.build();
    }
}
