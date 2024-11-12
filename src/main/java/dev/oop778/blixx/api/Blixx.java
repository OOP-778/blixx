package dev.oop778.blixx.api;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.parser.config.ParserConfig;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.placeholder.PlaceholderConfig;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.util.UnsafeCast;
import lombok.NonNull;

public interface Blixx {
    static BlixxBuilder.ParserSelectorPart<BlixxBuilder.PlaceholderSelectorPart<BlixxBuilder.OptionalStep>> builder() {
        return UnsafeCast.cast(new BlixxBuilder.Impl());
    }

    ParserConfig parserConfig();

    PlaceholderConfig placeholderConfig();

    BlixxComponent parse(@NonNull String input, PlaceholderContext context);

    BlixxNode parseIntoNode(@NonNull String input, PlaceholderContext context);

    default BlixxComponent parse(@NonNull String input) {
        return this.parse(input, null);
    }
}
