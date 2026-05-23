package dev.oop778.blixx.hytale;

import com.hypixel.hytale.server.core.Message;
import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxStyle;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.BlixxNodeWithPrebuilt;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNode;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HytaleNodeBuilder {
    public static Message build(BlixxNodeInternal root) {
        final Blixx blixx = root.blixx();
        final BlixxProcessor.Component.ComponentContext context = new BlixxProcessor.Component.ComponentContext(blixx);

        final List<Message> parts = new ArrayList<>();

        final Iterator<BlixxNodeInternal> iterator = root.iterator(true);

        while (iterator.hasNext()) {
            final BlixxNodeInternal node = iterator.next();

            if (!(node instanceof BlixxNodeWithPrebuilt)) {
                continue;
            }

            final Object preBuilt = ((BlixxNodeWithPrebuilt) node).getPreBuilt();
            if (preBuilt instanceof Message) {
                parts.add((Message) preBuilt);
                continue;
            }

            if (!(node instanceof BlixxTextNode)) {
                continue;
            }

            final BlixxTextNode textNode = (BlixxTextNode) node;
            context.setNode(node);

            final BlixxStyle blixxStyle = new BlixxStyle();
            context.setContent(textNode.getContent());
            context.setStyle(blixxStyle);

            // Gradient appender: collect per-character colored messages
            final List<Message> gradientParts = new ArrayList<>();
            context.setGradientCharacterAppender((character, color) -> {
                Message charMsg = Message.raw(String.valueOf(character));
                if (color != null) {
                    charMsg = charMsg.color(HytaleStyleConverter.convertColorToHex(color));
                }
                gradientParts.add(charMsg);
            });

            // Process tag processors
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

            if (!gradientParts.isEmpty()) {
                // Gradient was applied — use the per-character parts
                parts.add(Message.join(gradientParts.toArray(new Message[0])));
            } else {
                // Normal text node
                final String content = context.getContent();
                if (content != null && !content.isEmpty()) {
                    parts.add(HytaleStyleConverter.applyStyle(Message.raw(content), blixxStyle));
                }
            }
        }

        if (parts.isEmpty()) {
            return Message.empty();
        }

        if (parts.size() == 1) {
            return parts.get(0);
        }

        return Message.join(parts.toArray(new Message[0]));
    }
}
