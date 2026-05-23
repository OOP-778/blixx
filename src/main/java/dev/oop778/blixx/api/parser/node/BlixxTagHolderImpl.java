package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.parser.TagCopier;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.collection.ObjectArray;
import java.util.function.Predicate;

public class BlixxTagHolderImpl implements BlixxTagHolder {
    protected boolean hasIndexableTagData;
    protected ObjectArray<BlixxTag.WithDefinedData<?>> tags;

    public BlixxTagHolderImpl(Iterable<BlixxTag.WithDefinedData<?>> tags) {
        this(new ObjectArray<>(tags));
    }

    public BlixxTagHolderImpl(ObjectArray<BlixxTag.WithDefinedData<?>> tags) {
        this.tags = tags;

        if (this.tags != null) {
            for (final BlixxTag.WithDefinedData<?> tag : this.tags) {
                if (tag.getDefinedData() instanceof Indexable) {
                    this.hasIndexableTagData = true;
                    break;
                }
            }
        }
    }

    @Override
    public boolean hasIndexableTagData() {
        return this.hasIndexableTagData;
    }

    @Override
    public Iterable<BlixxTag.WithDefinedData<?>> getTags() {
        return this.tags == null ? ObjectArray.empty() : this.tags;
    }

    @Override
    public boolean hasTag(BlixxTag.WithDefinedData<?> parsedTag) {
        return this.tags != null && this.tags.stream().anyMatch(parsedTag::compare);
    }

    @Override
    public boolean hasTag(Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
        return this.tags != null && this.tags.stream().anyMatch(tagFilterer);
    }

    @Override
    public BlixxTag.WithDefinedData<?> findTag(Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
        return this.tags == null
                ? null
                : this.tags.stream().filter(tagFilterer).findFirst().orElse(null);
    }

    @Override
    public void setTags(Iterable<BlixxTag.WithDefinedData<?>> tags) {
        this.tags = new ObjectArray<>(tags);
    }

    @Override
    public void replaceOrAddTag(BlixxTag.WithDefinedData<?> tag) {
        if (this.tags == null) {
            this.tags = new ObjectArray<>(new BlixxTag.WithDefinedData[] {tag});
            return;
        }

        this.tags.replaceOrAdd((ctag) -> tag.getOriginalTag().equals(ctag), tag);
    }

    @Override
    public boolean hasTags() {
        return !this.tags.isEmpty();
    }

    public BlixxTagHolderImpl copy() {
        return new BlixxTagHolderImpl(TagCopier.copyTags(this.tags));
    }
}
