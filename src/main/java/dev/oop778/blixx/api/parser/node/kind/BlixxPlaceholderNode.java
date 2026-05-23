package dev.oop778.blixx.api.parser.node.kind;

import dev.oop778.blixx.api.parser.node.AbstractBlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.BlixxTagHolder;
import dev.oop778.blixx.api.parser.node.BlixxTagHolderImpl;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.collection.ObjectArray;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

@Getter
public class BlixxPlaceholderNode extends AbstractBlixxNode implements BlixxTagHolder {
    private final String placeholder;

    @Delegate
    private final BlixxTagHolderImpl tagHolder;

    @Setter
    private Object closingKey;

    @Setter
    private transient BlixxNodeInternal rootNodeReplacement;

    public BlixxPlaceholderNode(String placeholder, ObjectArray<BlixxTag.WithDefinedData<?>> tags) {
        this.tagHolder = new BlixxTagHolderImpl(tags);
        this.placeholder = placeholder;
    }

    public BlixxPlaceholderNode(BlixxPlaceholderNode from) {
        this.tagHolder = from.tagHolder.copy();
        this.placeholder = from.placeholder;
        this.closingKey = from.closingKey;
    }

    @Override
    public BlixxPlaceholderNode copyMe() {
        return new BlixxPlaceholderNode(this);
    }

    @Override
    public BlixxNodeInternal copy() {
        return super.copy();
    }
}
