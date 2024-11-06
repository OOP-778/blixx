package dev.oop778.blixx.api.parser;

import dev.oop778.blixx.api.tag.BlixxTag;
import lombok.Data;

@Data
public class TagWithDefinedDataImpl<T> implements BlixxTag.WithDefinedData<T>, BlixxTag.Wrapping<T> {
    private final BlixxTag<T> originalTag;
    private final T definedData;
}
