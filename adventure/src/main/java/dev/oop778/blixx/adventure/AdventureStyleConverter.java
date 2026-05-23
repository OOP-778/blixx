package dev.oop778.blixx.adventure;

import dev.oop778.blixx.adventure.util.StyleBuilder;
import dev.oop778.blixx.api.component.*;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

public class AdventureStyleConverter {
    private static final Map<String, TextDecoration> DECORATION_MAP = new HashMap<>();
    private static final Map<String, ClickEvent.Action> CLICK_ACTION_MAP = new HashMap<>();

    static {
        for (final TextDecoration decoration : TextDecoration.values()) {
            DECORATION_MAP.put(decoration.name().toLowerCase(), decoration);
        }

        CLICK_ACTION_MAP.put("open_url", ClickEvent.Action.OPEN_URL);
        CLICK_ACTION_MAP.put("run_command", ClickEvent.Action.RUN_COMMAND);
        CLICK_ACTION_MAP.put("suggest_command", ClickEvent.Action.SUGGEST_COMMAND);
        CLICK_ACTION_MAP.put("change_page", ClickEvent.Action.CHANGE_PAGE);
        CLICK_ACTION_MAP.put("copy_to_clipboard", ClickEvent.Action.COPY_TO_CLIPBOARD);
    }

    public static Style convert(BlixxStyle blixxStyle, @Nullable Style defaultStyle) {
        if (blixxStyle.isEmpty()) {
            return defaultStyle != null ? defaultStyle : Style.empty();
        }

        final StyleBuilder builder = new StyleBuilder(defaultStyle);

        if (blixxStyle.getColor() != null) {
            builder.color(convertColor(blixxStyle.getColor()));
        }

        for (final BlixxDecoration decoration : blixxStyle.getDecorations()) {
            final TextDecoration textDecoration = DECORATION_MAP.get(decoration.getName());
            if (textDecoration != null) {
                builder.decorate(textDecoration);
            }
        }

        if (blixxStyle.getClickEvent() != null) {
            final BlixxClickEvent clickEvent = blixxStyle.getClickEvent();
            final ClickEvent.Action action = CLICK_ACTION_MAP.get(clickEvent.getAction());
            if (action != null) {
                builder.clickEvent(ClickEvent.clickEvent(action, clickEvent.getValue()));
            }
        }

        if (blixxStyle.getHoverEvent() != null) {
            final BlixxHoverEvent hoverEvent = blixxStyle.getHoverEvent();
            if ("show_text".equals(hoverEvent.getAction())) {
                final BlixxNode node = hoverEvent.getNode();
                builder.hoverEvent(HoverEvent.showText(AdventureNodeBuilder.build((BlixxNodeInternal) node, null)));
            }
        }

        if (blixxStyle.getInsertion() != null) {
            builder.insertion(blixxStyle.getInsertion());
        }

        return builder.build();
    }

    public static TextColor convertColor(BlixxColor color) {
        return TextColor.color(color.getValue());
    }
}
