package dev.oop778.blixx.api.parser.node.identityspec;

import dev.oop778.blixx.api.parser.TagWithDefinedDataImpl;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.collection.ObjectArray;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class BlixxIdentityNodeImpl extends BlixxNodeImpl {
    public BlixxIdentityNodeImpl(BlixxIdentitySpec spec) {
        super(null, spec);
    }

    @Override
    public BlixxNodeImpl createNextNode(@Nullable Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
        final BlixxIdentityNodeImpl next = new BlixxIdentityNodeImpl(new BlixxIdentitySpec(this.spec.getBlixx()));
        if (this.tags != null) {
            next.tags = tagFilterer == null ? new ObjectArray<>(this.tags) : this.tags.filter(tagFilterer);
        }

        next.previous = this;
        this.next = next;

        return next;
    }

    @Override
    public BlixxIdentityNodeImpl copyMe() {
        final BlixxIdentityNodeImpl copy = new BlixxIdentityNodeImpl(null);

        final Map<Indexable, Indexable> alreadyCopied = new IdentityHashMap<>(((BlixxIdentitySpec) this.spec).getIndexables().size());
        for (final List<Indexable> indexableList : ((BlixxIdentitySpec) this.spec).getIndexables().values()) {
            for (final Indexable indexable : indexableList) {
                if (indexable == this) {
                    alreadyCopied.put(indexable, copy);
                    continue;
                }

                alreadyCopied.put(indexable, indexable.copy());
            }
        }

        copy.spec = ((BlixxIdentitySpec) this.spec).copy(alreadyCopied);
        copy.content = this.content;
        copy.tags = this.copyTags(alreadyCopied);
        copy.adventureComponent = this.adventureComponent;

        return copy;
    }

    @Override
    public void replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        new NodeReplacementIdentity(this, placeholders, context).work();
    }

    private ObjectArray<BlixxTag.WithDefinedData<?>> copyTags(Map<Indexable, Indexable> alreadyCopied) {
        if (!this.hasIndexableTagData) {
            return this.tags == null ? null : new ObjectArray<>(this.tags);
        }

        return this.tags.map(tag -> this.copyTag(tag, alreadyCopied));
    }

    @SuppressWarnings("unchecked")
    private <T> BlixxTag.WithDefinedData<T> copyTag(BlixxTag.WithDefinedData<T> tag, Map<Indexable, Indexable> alreadyCopied) {
        if (!(tag.getDefinedData() instanceof Indexable)) {
            return tag;
        }

        final Indexable indexable = (Indexable) tag.getDefinedData();
        final Indexable copy = alreadyCopied.get(indexable);
        return new TagWithDefinedDataImpl<>(tag, (T) (copy != null ? copy : indexable.copy()));
    }
}
