package dev.oop778.blixx.api;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.parser.config.ParserConfig;
import dev.oop778.blixx.api.placeholder.PlaceholderConfig;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.util.UnsafeCast;
import lombok.NonNull;

public interface Blixx {
    static BlixxBuilder.ParserSelectorPart<BlixxBuilder.PlaceholderSelectorPart<BlixxBuilder.OptionalStep>> builder() {
        return UnsafeCast.cast(new BlixxBuilder.Impl());
    }

    ParserConfig parserConfig();
    PlaceholderConfig placeholderConfig();

    default BlixxComponent parse(@NonNull String input) {
        return this.parse(input, null);
    }

    BlixxComponent parse(@NonNull String input, PlaceholderContext context);
}
