package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.tag.BlixxTag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class TagShortener<T> implements BlixxTag.Wrapping<T>, BlixxTag.WithDefinedData<T> {
    private final BlixxTag<T> originalTag;
    private final T definedData;
}
