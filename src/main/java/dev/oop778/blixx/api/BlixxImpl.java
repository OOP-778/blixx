package dev.oop778.blixx.api;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.BlixxComponentImpl;
import dev.oop778.blixx.api.parser.ParserImpl;
import dev.oop778.blixx.api.parser.config.ParserConfigImpl;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.placeholder.PlaceholderConfigImpl;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.platform.BlixxPlatform;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Getter
@Accessors(fluent = true)
public class BlixxImpl implements Blixx {
    private final ParserConfigImpl parserConfig;
    private final PlaceholderConfigImpl placeholderConfig;
    private final @Nullable BlixxPlatform platform;
    private final ParserImpl parser;

    public BlixxImpl(
            ParserConfigImpl parserConfig, PlaceholderConfigImpl placeholderConfig, @Nullable BlixxPlatform platform) {
        this.parserConfig = parserConfig;
        this.placeholderConfig = placeholderConfig;
        this.platform = platform;
        this.parser = new ParserImpl(this);
    }

    @Override
    public BlixxComponent parseComponent(@NonNull String input, PlaceholderContext context) {
        final BlixxNodeInternal parse = this.parser.parse(input, context);
        return new BlixxComponentImpl(parse);
    }

    @Override
    public BlixxNodeInternal parseNode(@NonNull String input, PlaceholderContext context) {
        return this.parser.parse(input, context);
    }
}
