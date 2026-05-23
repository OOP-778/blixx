package dev.oop778.blixx.hytale;

import com.hypixel.hytale.server.core.Message;
import dev.oop778.blixx.api.component.BlixxClickEvent;
import dev.oop778.blixx.api.component.BlixxColor;
import dev.oop778.blixx.api.component.BlixxDecoration;
import dev.oop778.blixx.api.component.BlixxStyle;
import java.util.HashMap;
import java.util.Map;

public class HytaleStyleConverter {
    private static final Map<String, String> DECORATION_METHODS = new HashMap<>();

    static {
        DECORATION_METHODS.put("bold", "bold");
        DECORATION_METHODS.put("italic", "italic");
        DECORATION_METHODS.put("underlined", "underlined");
        DECORATION_METHODS.put("monospace", "monospace");
    }

    public static Message applyStyle(Message message, BlixxStyle style) {
        if (style.isEmpty()) {
            return message;
        }

        if (style.getColor() != null) {
            message = message.color(convertColorToHex(style.getColor()));
        }

        for (final BlixxDecoration decoration : style.getDecorations()) {
            message = applyDecoration(message, decoration);
        }

        if (style.getClickEvent() != null) {
            final BlixxClickEvent clickEvent = style.getClickEvent();
            // Hytale only supports link (open_url equivalent)
            if ("open_url".equals(clickEvent.getAction())) {
                message = message.link(clickEvent.getValue());
            }
        }

        // Hytale does not support hover events — silently skip

        return message;
    }

    public static String convertColorToHex(BlixxColor color) {
        return color.asHexString();
    }

    private static Message applyDecoration(Message message, BlixxDecoration decoration) {
        switch (decoration.getName()) {
            case "bold":
                return message.bold(true);
            case "italic":
                return message.italic(true);
            case "monospace":
                return message.monospace(true);
            // Hytale does not have underline/strikethrough/obfuscated on Message API
            // underlined exists on FormattedMessage protocol but not on Message builder
            default:
                return message;
        }
    }
}
