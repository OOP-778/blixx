package dev.oop778.blixx.api.tag;

import dev.oop778.blixx.tag.decoration.*;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BlixxTags {
    Map<TextDecoration, List<String>> DECORATIONS = new HashMap<TextDecoration, List<String>>() {{
        this.put(TextDecoration.BOLD, Arrays.asList("bold", "b"));
        this.put(TextDecoration.UNDERLINED, Arrays.asList("underlined", "u"));
        this.put(TextDecoration.STRIKETHROUGH, Arrays.asList("strikethrough", "st"));
        this.put(TextDecoration.OBFUSCATED, Arrays.asList("obfuscated", "obf"));
        this.put(TextDecoration.ITALIC, Arrays.asList("italic", "em", "i"));
    }};

    Map<String, BlixxTag<?>> STANDARD = new HashMap<String, BlixxTag<?>>() {{
        this.put("color", ColorTag.INSTANCE);
        this.put("decorate", DecorationTag.INSTANCE);
        this.put("reset", ResetTag.INSTANCE);
        this.put("gradient", GradientTag.INSTANCE);
        this.put("hover", HoverTag.INSTANCE);
        this.put("click", ClickTag.INSTANCE);
        this.put("small_caps", SmallCapsTag.INSTANCE);
        this.put("sc", SmallCapsTag.INSTANCE);

        for (final Entry<TextDecoration, List<String>> decorationEntry : DECORATIONS.entrySet()) {
            final TagShortener<TextDecoration> tag = new TagShortener<>(DecorationTag.INSTANCE, decorationEntry.getKey());
            for (final String identifier : decorationEntry.getValue()) {
                this.put(identifier, tag);
            }
        }

        for (final NamedTextColor namedTextColor : NamedTextColor.NAMES.values()) {
            this.put(namedTextColor.toString(), new TagShortener<>(ColorTag.INSTANCE, namedTextColor));
        }
    }};
}
