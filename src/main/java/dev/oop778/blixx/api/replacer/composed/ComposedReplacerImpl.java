package dev.oop778.blixx.api.replacer.composed;

import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.action.ReplaceAction;
import dev.oop778.blixx.api.replacer.action.ReplaceActionImpl;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.util.UnsafeCast;
import dev.oop778.blixx.util.collection.ComposedIterator;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ComposedReplacerImpl implements ComposedReplacer {
    private final PlaceholderHolder<?>[] holders;
    private final Iterable<? extends BlixxPlaceholder<?>>[] composedHolders;

    public ComposedReplacerImpl(PlaceholderHolder<?>... holders) {
        this.holders = holders;
        this.composedHolders = this.flattenHolders(holders);
    }

    @Override
    public Iterable<? extends BlixxPlaceholder<?>> getPlaceholders() {
        return new Iterable<BlixxPlaceholder<?>>() {
            @Override
            public @NotNull Iterator<BlixxPlaceholder<?>> iterator() {
                return new ComposedIterator<>(ComposedReplacerImpl.this.composedHolders);
            }
        };
    }

    @Override
    public ComposedReplacer withPlaceholder(@NonNull Where where, BlixxPlaceholder<?> placeholder) {
        Replacer.create();
        return ComposedReplacer.create();
    }

    @Override
    public <T> ReplaceAction<T> accept(@NotNull T object) {
        return UnsafeCast.cast(new ReplaceActionImpl(object, this));
    }

    private Iterable<? extends BlixxPlaceholder<?>>[] flattenHolders(PlaceholderHolder<?>... holders) {
        final List<Iterable<? extends BlixxPlaceholder<?>>> flattened = new ArrayList<>(holders.length);
        final Queue<PlaceholderHolder<?>> toVisit = new LinkedList<>(Arrays.asList(holders));

        while (!toVisit.isEmpty()) {
            final PlaceholderHolder<?> poll = toVisit.poll();
            if (poll instanceof ComposedReplacerImpl) {
                toVisit.addAll(Arrays.asList(((ComposedReplacerImpl) poll).holders));
                continue;
            }

            flattened.add(poll.getPlaceholders());
        }

        return UnsafeCast.cast(flattened.toArray(new Iterable[0]));
    }
}
