package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

@RequiredArgsConstructor
@Getter
@ApiStatus.Internal
public class BlixxComponentImpl implements BlixxComponent {
    private final BlixxNodeImpl node;

    @Override
    public BlixxComponent replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        return this.actOnCopy(copy -> copy.doReplaceWithoutCopy(placeholders, context));
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
        return this.actOnCopy(copy -> {
            BlixxNodeImpl currentEndNode = copy.getNode().findTreeEnd();
            for (@NotNull @NonNull final BlixxComponent their : component) {
                final BlixxNodeImpl theirNode = (BlixxNodeImpl) their.copy().getNode();
                currentEndNode.setNext(theirNode);
                theirNode.setPrevious(currentEndNode);

                currentEndNode = theirNode;
            }

            return copy;
        });
    }

    @Override
    public BlixxComponent append(@NonNull Component component) {
        return this.append(BlixxComponent.wrap(component));
    }

    @Override
    public BlixxComponent append(@NonNull Iterable<BlixxComponent> components) {
        return this.actOnCopy(copy -> {
            BlixxNodeImpl currentEndNode = copy.getNode().findTreeEnd();
            for (@NotNull @NonNull final BlixxComponent their : components) {
                final BlixxNodeImpl theirNode = (BlixxNodeImpl) their.copy().getNode();
                currentEndNode.setNext(theirNode);
                theirNode.setPrevious(currentEndNode);

                currentEndNode = theirNode;
            }

            return copy;
        });
    }

    @Override
    public Component asComponent(Style defaultStyle) {
        return this.node.build(defaultStyle);
    }

    public BlixxComponentImpl doReplaceWithoutCopy(Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        this.node.replace(placeholders, context);
        return this;
    }

    public BlixxComponent actOnCopy(UnaryOperator<BlixxComponentImpl> operator) {
        return operator.apply((BlixxComponentImpl) this.copy());
    }
}
