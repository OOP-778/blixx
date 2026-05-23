package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.formatter.BlixxFormatters;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;

/** Configuration for placeholder resolution: default formatters and context. */
public interface PlaceholderConfig {
    /** Returns the formatters used to convert non-string values to text, or null if none. */
    BlixxFormatters defaultFormatters();

    /** Returns the default context supplied to all placeholder resolutions, or null if none. */
    PlaceholderContext defaultContext();
}
