package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.node.BlixxNode;
import lombok.Getter;

@Getter
/** Platform-agnostic hover event. Currently supports the "show_text" action with a {@link BlixxNode} tooltip. */
public class BlixxHoverEvent {
    private final String action;
    private final BlixxNode node;

    private BlixxHoverEvent(String action, BlixxNode node) {
        this.action = action;
        this.node = node;
    }

    public static BlixxHoverEvent showText(BlixxNode node) {
        return new BlixxHoverEvent("show_text", node);
    }

    @Override
    public String toString() {
        return "BlixxHoverEvent{action=" + this.action + "}";
    }
}
