package dev.oop778.blixx.tag.decoration;

import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.ObjectArray;

import java.util.List;

public class ResetTag implements BlixxTag.NoData, BlixxProcessor.Tree.Filterer {
    public static final ResetTag INSTANCE = new ResetTag();

    @Override
    public BlixxProcessor getProcessor() {
        return this;
    }

    @Override
    public ObjectArray<WithDefinedData<?>> filter(Context context, ObjectArray<? extends WithDefinedData<?>> tags) {
        return new ObjectArray<>(0);
    }
}
