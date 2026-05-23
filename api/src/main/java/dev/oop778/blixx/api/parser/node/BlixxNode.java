package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

/** A node in the parsed text tree. Nodes form a doubly-linked list and carry tags and content. */
public interface BlixxNode {
    /** Returns the Blixx instance that parsed this node. */
    Blixx blixx();

    /** Creates an independent deep copy of this node and all subsequent nodes. */
    @CheckReturnValue
    BlixxNode copy();

    /** Returns the next node in the linked list, or null. */
    BlixxNode getNext();

    /** Returns the previous node in the linked list, or null. */
    BlixxNode getPrevious();

    /** Replaces placeholders in this node tree in place. */
    void replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, @Nullable PlaceholderContext context);
}
