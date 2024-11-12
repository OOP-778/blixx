package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class BlixxComponentImpl implements BlixxComponent {
    private final BlixxNodeImpl node;

    @Override
    public BlixxComponent replace(List<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        final BlixxNodeImpl copy = this.node.copy();
        copy.replace(placeholders, context);
        return new BlixxComponentImpl(copy);
    }

    @Override
    public @NotNull Component asComponent() {
        return this.node.getAdventureComponent();
    }

    public BlixxComponent copy() {
        return new BlixxComponentImpl(this.node.copy());
    }

    @Override
    public BlixxComponent append(@NotNull @NonNull BlixxComponent... component) {
        final BlixxComponent us = this.copy();
        final BlixxNodeImpl currentEndNode = ((BlixxNodeImpl) us.getNode()).findTreeEnd();

        for (@NotNull @NonNull final BlixxComponent their : component) {
            final BlixxNodeImpl theirNode = (BlixxNodeImpl) their.copy().getNode();
            theirNode.setPrevious(currentEndNode);

        }

        return us;
    }

    @Override
    public BlixxComponent append(@NonNull Iterable<BlixxComponent> components) {
        final BlixxComponent us = this.copy();
        final BlixxNodeImpl currentEndNode = ((BlixxNodeImpl) us.getNode()).findTreeEnd();

        for (@NotNull @NonNull final BlixxComponent their : components) {
            final BlixxNodeImpl theirNode = (BlixxNodeImpl) their.copy().getNode();
            theirNode.setPrevious(currentEndNode);

        }

        return us;
    }
}
