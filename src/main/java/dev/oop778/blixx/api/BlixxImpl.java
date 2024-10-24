package dev.oop778.blixx.api;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.BlixxComponentImpl;
import dev.oop778.blixx.api.parser.BlixxNodeImpl;
import dev.oop778.blixx.api.parser.ParserImpl;
import dev.oop778.blixx.api.parser.config.ParserConfigImpl;
import dev.oop778.blixx.api.placeholder.PlaceholderConfigImpl;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Getter
@Accessors(fluent = true)
public class BlixxImpl implements Blixx {
    private final ParserConfigImpl parserConfig;
    private final PlaceholderConfigImpl placeholderConfig;
    private final ParserImpl parser;

    public BlixxImpl(ParserConfigImpl parserConfig, PlaceholderConfigImpl placeholderConfig) {
        this.parserConfig = parserConfig;
        this.placeholderConfig = placeholderConfig;
        this.parser = new ParserImpl(this);
    }

    @Override
    public BlixxComponent parse(@NonNull String input, PlaceholderContext context) {
        final BlixxNodeImpl parse = this.parser.parse(input, context);
        return new BlixxComponentImpl(parse);
    }
}
