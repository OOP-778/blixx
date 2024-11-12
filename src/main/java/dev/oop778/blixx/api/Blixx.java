package dev.oop778.blixx.api;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.parser.config.ParserConfig;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.placeholder.PlaceholderConfig;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.util.ThreadSafeLazyInit;
import dev.oop778.blixx.util.UnsafeCast;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public interface Blixx {
    static BlixxBuilder.ParserSelectorPart<BlixxBuilder.PlaceholderSelectorPart<BlixxBuilder.OptionalStep>> builder() {
        return UnsafeCast.cast(new BlixxBuilder.Impl());
    }

    static Blixx standard() {
        return Helper.STANDARD_BLIXX.get();
    }

    ParserConfig parserConfig();
    PlaceholderConfig placeholderConfig();

    BlixxComponent parseComponent(@NonNull String input, @Nullable PlaceholderContext context);

    BlixxNode parseNode(@NonNull String input, @Nullable PlaceholderContext context);

    default BlixxComponent parseComponent(@NonNull String input) {
        return this.parseComponent(input, null);
    }

    default BlixxNode parseNode(@NonNull String input) {
        return this.parseNode(input, null);
    }

    class Helper {
        private static final ThreadSafeLazyInit<Blixx> STANDARD_BLIXX = new ThreadSafeLazyInit<>(() -> builder().withStandardParserConfig().withStandardPlaceholderConfig().build());
    }
}
