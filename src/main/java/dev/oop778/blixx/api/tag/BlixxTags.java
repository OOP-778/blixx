package dev.oop778.blixx.api.tag;

import dev.oop778.blixx.tag.decoration.*;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashMap;
import java.util.Map;

public interface BlixxTags {
    Map<String, BlixxTag<?>> STANDARD = new HashMap<String, BlixxTag<?>>() {{
        this.put("color", ColorTag.INSTANCE);
        this.put("decorate", DecorationTag.INSTANCE);
        this.put("reset", ResetTag.INSTANCE);
        this.put("gradient", GradientTag.INSTANCE);
        this.put("hover", HoverTag.INSTANCE);
        this.put("click", ClickTag.INSTANCE);

        for (final TextDecoration value : TextDecoration.values()) {
            this.put(value.name().toLowerCase(), new TagShortener<>(DecorationTag.INSTANCE, value));
        }

        for (final NamedTextColor namedTextColor : NamedTextColor.NAMES.values()) {
            this.put(namedTextColor.toString(), new TagShortener<>(ColorTag.INSTANCE, namedTextColor));
        }
    }};

}
