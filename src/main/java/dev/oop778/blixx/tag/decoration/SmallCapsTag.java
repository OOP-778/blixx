package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import lombok.NonNull;
import net.kyori.adventure.text.format.TextDecoration;

public class SmallCapsTag implements BlixxTag.NoData {
    private static final Processor PROCESSOR = new Processor();
    public static SmallCapsTag INSTANCE = new SmallCapsTag();

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<TextDecoration> {
        private static final char[] SMALL_CAPS_ALPHABET = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴩqʀꜱᴛᴜᴠᴡxyᴢ".toCharArray();

        @Override
        public void decorate(@NonNull ComponentContext context) {
            context.getComponentBuilder().setContent(this.convert(context.getComponentBuilder().getContent()));
        }

        public String convert(String input) {
            if (null == input) {
                return null;
            }

            final int length = input.length();
            final StringBuilder smallCaps = new StringBuilder(length);

            for (int i = 0; i < length; ++i) {
                final char c = input.charAt(i);
                if (c >= 'a' && c <= 'z') {
                    smallCaps.append(SMALL_CAPS_ALPHABET[c - 'a']);
                } else {
                    smallCaps.append(c);
                }
            }

            return smallCaps.toString();
        }
    }
}
