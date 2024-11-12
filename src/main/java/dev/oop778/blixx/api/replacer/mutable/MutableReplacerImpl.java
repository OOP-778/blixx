package dev.oop778.blixx.api.replacer.mutable;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.immutable.ReplacerImpl;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MutableReplacerImpl implements MutableReplacer{
    private final Blixx blixx;
    private final List<BlixxPlaceholder<?>> placeholders;

    public MutableReplacerImpl(@Nullable Blixx blixx) {
        this.blixx = blixx;
        this.placeholders = new ArrayList<>();
    }

    @Override
    public Replacer toImmutable() {
        return new ReplacerImpl(
                this.blixx,
                new ArrayList<>(this.placeholders)
        );
    }

    @Override
    public Iterable<? extends BlixxPlaceholder<?>> getPlaceholders() {
        return this.placeholders;
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
    public <T> T accept(@NotNull T object, @Nullable PlaceholderContext context) {
        return null;
    }
}
