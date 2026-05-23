package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import lombok.NonNull;

public class SmallCapsTag implements BlixxTag.NoData {
    private static final Processor PROCESSOR = new Processor();
    public static SmallCapsTag INSTANCE = new SmallCapsTag();

    @Override
    public BlixxProcessor getProcessor() {
        return PROCESSOR;
    }

    public static class Processor implements BlixxProcessor.Component.Decorator<Void> {
        private static final char[] SMALL_CAPS_ALPHABET =
                "\u1D00\u0299\u1D04\u1D05\u1D07\uA730\u0262\u029C\u026A\u1D0A\u1D0B\u029F\u1D0D\u0274\u1D0F\u1D29q\u0280\uA731\u1D1B\u1D1C\u1D20\u1D21xy\u1D22"
                        .toCharArray();

        @Override
        public void decorate(@NonNull ComponentContext context) {
            context.setContent(this.convert(context.getContent()));
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
