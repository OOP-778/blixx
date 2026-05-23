package dev.oop778.blixx.hytale;

import com.hypixel.hytale.server.core.Message;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;

public class HytaleBlixxComponent {
    public static Message asMessage(BlixxComponent component) {
        return HytaleNodeBuilder.build((BlixxNodeInternal) component.getNode());
    }
}
