package dev.oop778.blixx.hytale;

import com.hypixel.hytale.server.core.Message;
import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.BlixxStyle;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNode;
import dev.oop778.blixx.api.platform.BlixxPlatform;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import org.jetbrains.annotations.Nullable;

public class HytaleBlixx implements BlixxPlatform {
    public static final HytaleBlixx INSTANCE = new HytaleBlixx();

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

        return HytaleStyleConverter.applyStyle(Message.raw(context.getContent()), blixxStyle);
    }

    @Override
    public Message build(BlixxComponent root) {
        return HytaleNodeBuilder.build((BlixxNodeInternal) root.getNode());
    }
}
