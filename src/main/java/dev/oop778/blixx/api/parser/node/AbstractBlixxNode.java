package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.replacement.UniversalNodeReplacement;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
/** Base implementation for nodes in the parsed text tree. Handles linked-list traversal, copying, and replacement. */
public abstract class AbstractBlixxNode implements Indexable<BlixxNodeInternal>, BlixxNodeInternal {
    @Getter(lombok.AccessLevel.NONE)
    @Setter
    protected Blixx blixx;

    protected BlixxNodeInternal next;
    protected BlixxNodeInternal previous;

    @Override
    public Blixx blixx() {
        return this.blixx;
    }

    @Override
    public BlixxNodeInternal copy() {
        BlixxNodeInternal top = null;
        BlixxNodeInternal current = this;
        BlixxNodeInternal last = null;

        while (current != null) {
            final BlixxNodeInternal copy = current.copyMe();
            copy.setBlixx(current.blixx());

            if (top == null) {
                top = copy;
            }

            if (last != null) {
                last.setNext(copy);
                copy.setPrevious(last);
            }

            current = current.getNext();
            last = copy;
        }

        return top;
    }

    @Override
    public void setPrevious(BlixxNode previous) {
        this.previous = (BlixxNodeInternal) previous;
    }

    @Override
    public void setNext(BlixxNode next) {
        this.next = (BlixxNodeInternal) next;
    }

    @Override
    public void replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, @Nullable PlaceholderContext context) {
        new UniversalNodeReplacement(this, placeholders, context).work();
    }

    public BlixxNodeInternal findTreeEnd() {
        BlixxNodeInternal currentNode = this;
        while (currentNode.getNext() != null) {
            currentNode = currentNode.getNext();
        }

        return currentNode;
    }

    public Iterator<BlixxNodeInternal> iterator(boolean withItself) {
        return new Iterator<BlixxNodeInternal>() {
            private BlixxNodeInternal current = withItself ? AbstractBlixxNode.this : AbstractBlixxNode.this.next;

            @Override
            public boolean hasNext() {
                return this.current != null;
            }

            @Override
            public BlixxNodeInternal next() {
                if (this.current == null) {
                    throw new NoSuchElementException();
                }

                final BlixxNodeInternal nextNode = this.current;
                this.current = this.current.getNext();

                return nextNode;
            }
        };
    }
}
