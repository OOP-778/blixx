package dev.oop778.blixx.api.replacer.mutable;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.action.ReplaceAction;
import dev.oop778.blixx.api.replacer.action.ReplaceActionImpl;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.immutable.ReplacerImpl;
import dev.oop778.blixx.util.UnsafeCast;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MutableReplacerImpl implements MutableReplacer {
    private final List<BlixxPlaceholder<?>> placeholders;

    public MutableReplacerImpl() {
        this(new ArrayList<>(2));
    }

    public MutableReplacerImpl(ArrayList<BlixxPlaceholder<?>> placeholders) {
        this.placeholders = placeholders;
    }

    @Override
    public Replacer toImmutable() {
        return new ReplacerImpl(new ArrayList<>(this.placeholders));
    }

    @Override
    public Iterable<? extends BlixxPlaceholder<?>> getPlaceholders() {
        return Collections.unmodifiableList(this.placeholders);
    }

    @Override
    public MutableReplacer withPlaceholder(@NonNull Where where, BlixxPlaceholder<?> placeholder) {
        if (where == Where.START) {
            this.placeholders.add(0, placeholder);
        } else {
            this.placeholders.add(placeholder);
        }

        return this;
    }

    @Override
    public <T> ReplaceAction<T> accept(@NotNull T object) {
        return UnsafeCast.cast(new ReplaceActionImpl(object, this));
    }
}
