package dev.oop778.blixx.api.replacer.immutable;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ReplacerImpl implements Replacer {
    private final Blixx blixx;
    private final List<BlixxPlaceholder<?>> placeholders;

    public ReplacerImpl(@Nullable Blixx blixx) {
        this(blixx, new ArrayList<>(2));
    }

    public ReplacerImpl(@Nullable Blixx blixx, List<BlixxPlaceholder<?>> placeholders) {
        this.blixx = blixx;
        this.placeholders = placeholders;
    }

    public ReplacerImpl(@Nullable Blixx blixx, @Nullable BlixxPlaceholder<?>[] placeholders) {
        this(blixx, Arrays.asList(placeholders));
    }

    @Override
    public MutableReplacer toMutable() {
        return null;
    }

    @Override
    public <T> T accept(@NotNull T object, @Nullable PlaceholderContext context) {
        return null;
    }

    @Override
    public List<BlixxPlaceholder<?>> getPlaceholders() {
        return Collections.unmodifiableList(this.placeholders);
    }

    @Override
    public Replacer withPlaceholder(@NonNull Where where, BlixxPlaceholder<?> placeholder) {
        return this.modifyAction((copy) -> {
            if (where == Where.START) {
                copy.placeholders.add(0, placeholder);
            } else {
                copy.placeholders.add(placeholder);
            }
        });
    }

    public Replacer modifyAction(Consumer<ReplacerImpl> consumer) {
        final ReplacerImpl copy = new ReplacerImpl(this.blixx, new ArrayList<>(this.placeholders));
        consumer.accept(copy);
        return copy;
    }
}
