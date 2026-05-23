package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.kind.BlixxPlaceholderNode;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@ApiStatus.Internal
public class BlixxComponentImpl implements BlixxComponent {
    private BlixxNodeInternal node;

    public BlixxComponentImpl(BlixxNode node) {
        this.node = (BlixxNodeInternal) node;
    }

    @Override
    public BlixxComponent replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        return this.actOnCopy(copy -> copy.doReplaceWithoutCopy(placeholders, context));
    }

    public BlixxComponent copy() {
        return new BlixxComponentImpl(this.node.copy());
    }

    @Override
    public BlixxComponent append(@NotNull @NonNull BlixxComponent... component) {
        return this.actOnCopy(copy -> {
            BlixxNodeInternal currentEndNode = copy.getNode().findTreeEnd();
            for (@NotNull @NonNull final BlixxComponent their : component) {
                final BlixxNodeInternal theirNode =
                        (BlixxNodeInternal) their.copy().getNode();
                currentEndNode.setNext(theirNode);
                theirNode.setPrevious(currentEndNode);

                currentEndNode = theirNode;
            }

            return copy;
        });
    }

    @Override
    public BlixxComponent append(@NonNull Iterable<BlixxComponent> components) {
        return this.actOnCopy(copy -> {
            BlixxNodeInternal currentEndNode = copy.getNode().findTreeEnd();
            for (@NotNull @NonNull final BlixxComponent their : components) {
                final BlixxNodeInternal theirNode =
                        (BlixxNodeInternal) their.copy().getNode();
                currentEndNode.setNext(theirNode);
                theirNode.setPrevious(currentEndNode);

                currentEndNode = theirNode;
            }

            return copy;
        });
    }

    public BlixxComponentImpl doReplaceWithoutCopy(
            Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        this.node.replace(placeholders, context);

        while (this.node instanceof BlixxPlaceholderNode
                && ((BlixxPlaceholderNode) this.node).getRootNodeReplacement() != null) {
            this.node = ((BlixxPlaceholderNode) this.node).getRootNodeReplacement();
        }

        return this;
    }

    public BlixxComponent actOnCopy(UnaryOperator<BlixxComponentImpl> operator) {
        return operator.apply((BlixxComponentImpl) this.copy());
    }
}
