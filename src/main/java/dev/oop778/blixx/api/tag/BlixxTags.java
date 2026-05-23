package dev.oop778.blixx.api.tag;

import dev.oop778.blixx.api.component.BlixxColor;
import dev.oop778.blixx.api.component.BlixxDecoration;
import dev.oop778.blixx.tag.decoration.*;
import java.util.*;

/** Registry of all built-in tags: 16 named colors, 5 decorations with aliases, gradient, hover, click, reset, and small caps. */
public interface BlixxTags {
    /** Decoration-to-alias mapping (e.g., bold -> ["bold", "b"]). */
    Map<BlixxDecoration, List<String>> DECORATIONS = new HashMap<BlixxDecoration, List<String>>() {
        {
            this.put(BlixxDecoration.of("bold"), Arrays.asList("bold", "b"));
            this.put(BlixxDecoration.of("underlined"), Arrays.asList("underlined", "u"));
            this.put(BlixxDecoration.of("strikethrough"), Arrays.asList("strikethrough", "st"));
            this.put(BlixxDecoration.of("obfuscated"), Arrays.asList("obfuscated", "obf"));
            this.put(BlixxDecoration.of("italic"), Arrays.asList("italic", "em", "i"));
        }
    };

    /** All standard tags keyed by name. Includes colors, decorations, gradient, hover, click, reset, and small caps. */
    Map<String, BlixxTag<?>> STANDARD = new HashMap<String, BlixxTag<?>>() {
        {
            this.put("color", ColorTag.INSTANCE);
            this.put("decorate", DecorationTag.INSTANCE);
            this.put("reset", ResetTag.INSTANCE);
            this.put("gradient", GradientTag.INSTANCE);
            this.put("hover", HoverTag.INSTANCE);
            this.put("click", ClickTag.INSTANCE);
            this.put("small_caps", SmallCapsTag.INSTANCE);
            this.put("sc", SmallCapsTag.INSTANCE);

            for (final Entry<BlixxDecoration, List<String>> decorationEntry : DECORATIONS.entrySet()) {
                final TagShortener<BlixxDecoration> tag =
                        new TagShortener<>(DecorationTag.INSTANCE, decorationEntry.getKey());
                for (final String identifier : decorationEntry.getValue()) {
                    this.put(identifier, tag);
                }
            }

            for (final Map.Entry<String, BlixxColor> entry : NamedColors.all().entrySet()) {
                this.put(entry.getKey(), new TagShortener<>(ColorTag.INSTANCE, entry.getValue()));
            }
        }
    };
}
