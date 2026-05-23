package dev.oop778.blixx.api.parser;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.collection.ObjectArray;

public class TagCopier {
    public static ObjectArray<BlixxTag.WithDefinedData<?>> copyTags(ObjectArray<BlixxTag.WithDefinedData<?>> tags) {
        return tags.copy(TagCopier::copyTag);
    }

    private static <T> BlixxTag.WithDefinedData<T> copyTag(BlixxTag.WithDefinedData<T> tag) {
        if (tag.getDefinedData() instanceof Indexable) {
            return new TagWithDefinedDataImpl<>(tag.getOriginalTag(), (T) ((Indexable<?>) tag.getDefinedData()).copy());
        }

        return tag;
    }
}
