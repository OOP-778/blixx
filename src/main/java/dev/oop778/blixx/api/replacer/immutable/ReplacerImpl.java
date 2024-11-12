package dev.oop778.blixx.api.replacer.immutable;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.action.ReplaceAction;
import dev.oop778.blixx.api.replacer.action.ReplaceActionImpl;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacerImpl;
import dev.oop778.blixx.util.UnsafeCast;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ReplacerImpl implements Replacer {
    private final List<BlixxPlaceholder<?>> placeholders;

    public ReplacerImpl() {
        this(new ArrayList<>(2));
    }

    public ReplacerImpl(List<BlixxPlaceholder<?>> placeholders) {
        this.placeholders = placeholders;
    }

    public ReplacerImpl(@Nullable BlixxPlaceholder<?>[] placeholders) {
        this(Arrays.asList(placeholders));
    }

    @Override
    public MutableReplacer toMutable() {
        return new MutableReplacerImpl(new ArrayList<>(this.placeholders));
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

    @Override
    public <T> ReplaceAction<T> accept(@NotNull T object) {
        return UnsafeCast.cast(new ReplaceActionImpl(object, this));
    }

    public Replacer modifyAction(Consumer<ReplacerImpl> consumer) {
        final ReplacerImpl copy = new ReplacerImpl(new ArrayList<>(this.placeholders));
        consumer.accept(copy);
        return copy;
    }
}
