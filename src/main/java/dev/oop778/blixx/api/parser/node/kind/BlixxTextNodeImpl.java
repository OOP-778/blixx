package dev.oop778.blixx.api.parser.node.kind;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.*;
import dev.oop778.blixx.api.tag.BlixxTag;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
public class BlixxTextNodeImpl extends AbstractBlixxNode
        implements BlixxTextNode,
                Indexable.WithStringContent<BlixxNodeInternal>,
                BlixxTagHolder,
                BlixxNodeWithPrebuilt {
    @Delegate
    private final BlixxTagHolderImpl tagHolder;

    private String content;
    private Object built;

    public BlixxTextNodeImpl(String content, Iterable<BlixxTag.WithDefinedData<?>> tags) {
        this.content = content;
        this.tagHolder = new BlixxTagHolderImpl(tags);
    }

    protected BlixxTextNodeImpl(BlixxTextNodeImpl node) {
        this.content = node.content;
        this.tagHolder = node.tagHolder.copy();
        this.built = node.built;
    }

    @Override
    public BlixxTextNodeImpl copyMe() {
        return new BlixxTextNodeImpl(this);
    }

    @Override
    public BlixxNodeInternal copy() {
        return super.copy();
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public Object getPreBuilt() {
        return this.built;
    }

    @Override
    public void setPreBuilt(Object built) {
        this.built = built;
    }

    @Override
    public void setTags(Iterable<BlixxTag.WithDefinedData<?>> tags) {
        this.tagHolder.setTags(tags);
        this.built = null;
    }

    @Override
    public void replaceOrAddTag(BlixxTag.WithDefinedData<?> tag) {
        this.tagHolder.replaceOrAddTag(tag);
        this.built = null;
    }

    @Override
    public void prebuild() {}
}
