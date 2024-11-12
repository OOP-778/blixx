package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholderBuilder;
import lombok.NonNull;
import org.jetbrains.annotations.CheckReturnValue;

public interface ImmutableReplacer extends ReplacerAcceptable {
    @CheckReturnValue
    <T> ImmutableReplacer addPlaceholder(@NonNull BlixxPlaceholder<T> placeholder);

    @CheckReturnValue
    Replacer toMutable();

    @CheckReturnValue
    default <T> BlixxPlaceholderBuilder.SelectorStage<ImmutableReplacer, T> addPlaceholder() {
        return new BlixxPlaceholderBuilder.SelectorStageImpl<>((placeholder) -> this.addPlaceholder(placeholder));
    }
}
