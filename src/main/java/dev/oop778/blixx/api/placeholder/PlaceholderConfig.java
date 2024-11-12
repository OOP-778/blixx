package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.formatter.BlixxFormatters;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;

public interface PlaceholderConfig {
    BlixxFormatters defaultFormatters();

    PlaceholderContext defaultContext();
}
