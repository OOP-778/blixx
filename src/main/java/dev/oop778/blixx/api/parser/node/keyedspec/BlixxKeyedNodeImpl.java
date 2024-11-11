package dev.oop778.blixx.api.parser.node.keyedspec;

import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.ObjectArray;
import org.jetbrains.annotations.Nullable;
import java.util.function.Predicate;

public class BlixxKeyedNodeImpl extends BlixxNodeImpl {

    public BlixxKeyedNodeImpl(Object key, BlixxKeyedNodeSpec spec) {
        super(key, spec);
    }

    @Override
    public BlixxNodeImpl createNextNode(@Nullable Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
        final BlixxKeyedNodeImpl next = new BlixxKeyedNodeImpl(this.spec.createNodeKey(), (BlixxKeyedNodeSpec) this.spec);
        if (this.tags != null) {
            next.tags = tagFilterer == null ? new ObjectArray<>(this.tags) : this.tags.filter(tagFilterer);
        }

        this.next = next;
        next.previous = this;
        return next;
    }

    @Override
    public BlixxNodeImpl copyMe() {
        final BlixxKeyedNodeImpl copy = new BlixxKeyedNodeImpl(this.key, (BlixxKeyedNodeSpec) this.spec);
        copy.content = this.content;
        copy.tags = this.copyTags();
        copy.adventureComponent = this.adventureComponent;
        copy.hasIndexableTagData = this.hasIndexableTagData;

        return copy;
    }
}
