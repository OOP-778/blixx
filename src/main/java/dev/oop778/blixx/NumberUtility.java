package dev.oop778.blixx;

import lombok.Builder;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberUtility {
    private static final String[] m = {"", "M", "MM", "MMM"};
    private static final String[] c = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    private static final String[] x = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private static final String[] i = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    private enum Abbreviation {
        K("thousand"),
        M("million"),
        B("billion"),
        T("trillion"),
        Qa("quadrillion"),
        Q("quintillion");
        private final String fullName;
        private final double value;

        Abbreviation(String fullName) {
            this.fullName = fullName;
            this.value = Math.pow(10, (this.ordinal() + 1) * 3);
        }
    }

    public static class Parser {
        private static final Pattern REGEX_SUFFIX = Pattern.compile("([\\d|.|,]+)(\\D+)");
        private static final Pattern REGEX_NUMBER = Pattern.compile("([\\d|.|,]+)");

        public static long parseLong(String input) {
            return parse(input).longValue();
        }

        public static Number parse(String input) {
            final boolean neg = input.startsWith("-");
            if (neg) {
                input = input.substring(1);
            }

            if (REGEX_NUMBER.matcher(input).find()) {
                return (neg ? -1 : 1) * Double.parseDouble(input.replace(",", "."));
            }

            input = input.replaceAll(" ", "");
            double number = 0;
            final Matcher matcher = REGEX_SUFFIX.matcher(input);

            boolean match = false;
            while (matcher.find()) {
                match = true;
                final String groupNumber = matcher.group(1);
                final String groupSuffix = matcher.group(2);

                final double parsedNumber = Double.parseDouble(groupNumber.replace(",", "."));
                final Abbreviation abbreviation = Abbreviation.valueOf(groupSuffix.toUpperCase(Locale.ROOT));

                number += parsedNumber * abbreviation.value;
            }

            if (!match) {
                throw new IllegalStateException("Invalid input! Expected: [number][suffix].. or [number], Got: " + input);
            }

            return (neg ? -1 : 1) * number;
        }

        public static int parseInt(String input) {
            return parse(input).intValue();
        }

        public static double parseDouble(String input) {
            return parse(input).doubleValue();
        }
    }

    public static class Formatter {
        private static final Map<Integer, NumberFormatter> NUMBER_FORMATTERS = new ConcurrentHashMap<>();

        public static String formatShort(Number number, boolean longNames) {
            return format(number, true, longNames);
        }

        // Full: 1K 200
        // Full Long names: 1 thousand 200
        // Short: 1.2K
        // Short Long names: 1.2 thousand
        public static String format(Number number, boolean shortFormat, boolean longNames) {
            return getFormatter(shortFormat, longNames).format(number.doubleValue());
        }

        private static NumberFormatter getFormatter(boolean shortFormat, boolean longNames) {
            final int hash = Objects.hash(shortFormat, longNames, 3);
            return NUMBER_FORMATTERS.computeIfAbsent(hash, key -> NumberFormatter.builder()
                    .shortFormat(shortFormat)
                    .useAbbreviationAliases(!longNames)
                    .formatter(number -> DecimalFormat.getNumberInstance().format(number))
                    .build());
        }

        public static String formatShortLong(Number number) {
            return getFormatter(true, true).format(number.doubleValue());
        }

        public static String formatFullShort(Number number) {
            return getFormatter(false, false).format(number.doubleValue());
        }

        public static String formatFullLong(Number number) {
            return getFormatter(false, true).format(number.doubleValue());
        }

        public static String formatRoman(Number number) {
            return formatRoman(number.intValue());
        }

        public static String formatRoman(int number) {
            final String thousands = m[number / 1000];
            final String hundreds = c[(number % 1000) / 100];
            final String tens = x[(number % 100) / 10];
            final String ones = i[number % 10];

            return thousands + hundreds + tens + ones;
        }

        public static String formatWithCommas(Number number) {
            final String numberString = String.valueOf(number);
            if (numberString.length() <= 3) {
                return numberString;
            }

            final StringBuilder builder = new StringBuilder();
            for (int i = numberString.length() - 1; i >= 0; i--) {
                final int index = numberString.length() - i - 1;
                builder.append(numberString.charAt(i));

                if (index % 3 == 2 && index != numberString.length() - 1) {
                    builder.append(",");
                }
            }

            return builder.reverse().toString();
        }

        private static class NumberFormatter {
            private final boolean useAbbreviationAliases;
            private final boolean shortFormat;
            private final Function<Number, String> formatter;

            @Builder
            public NumberFormatter(boolean useAbbreviationAliases, boolean shortFormat, Function<Number, String> formatter) {
                this.useAbbreviationAliases = useAbbreviationAliases;
                this.shortFormat = shortFormat;
                this.formatter = formatter;
            }

            public String format(double v) {
                if (v < 1000) {
                    return this.formatter.apply(v);
                }

                return this.shortFormat ? this.formatShort(v) : this.formatFull(v);
            }

            public String formatFull(double v) {
                final StringBuilder builder = new StringBuilder();
                for (int i = Abbreviation.values().length - 1; i >= 0; i--) {
                    final Abbreviation abbreviation = Abbreviation.values()[i];
                    final int value = (int) Math.round(v / abbreviation.value);

                    if (value < 1) {
                        continue;
                    }

                    builder.append(this.formatter.apply(value)).append(" ").append(!this.useAbbreviationAliases ? abbreviation.fullName : abbreviation.name());
                    builder.append(" ");
                    v -= value * abbreviation.value;
                }

                return builder.toString();
            }

            public String formatShort(double value) {
                final int abbreviationIndex = (int) Math.ceil(Math.log10(value) / 3) - 2;

                final Abbreviation abbreviation = Abbreviation.values()[Math.max(0, abbreviationIndex)];
                return this.formatter.apply(value / abbreviation.value) + (!this.useAbbreviationAliases ? " " + abbreviation.fullName : abbreviation.name());
            }
        }
    }
}
