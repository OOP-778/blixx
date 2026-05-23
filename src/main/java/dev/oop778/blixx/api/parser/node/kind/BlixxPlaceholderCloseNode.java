package dev.oop778.blixx.api.parser.node.kind;

import dev.oop778.blixx.api.parser.node.AbstractBlixxNode;
import lombok.Getter;

@Getter
public class BlixxPlaceholderCloseNode extends AbstractBlixxNode {
    private final Object identityObject;

    public BlixxPlaceholderCloseNode(Object identityObject) {
        this.identityObject = identityObject;
    }

    @Override
    public BlixxPlaceholderCloseNode copyMe() {
        return new BlixxPlaceholderCloseNode(this.identityObject);
    }
}
