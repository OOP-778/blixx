package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.formatter.BlixxFormatters;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class PlaceholderConfigImpl implements PlaceholderConfig {
    private final BlixxFormatters defaultFormatters;
    private final PlaceholderContext defaultContext;
}
