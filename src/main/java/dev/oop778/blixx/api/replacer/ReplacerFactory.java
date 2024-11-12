package dev.oop778.blixx.api.replacer;

import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;

public interface ReplacerFactory {
    Replacer createImmutable();
    MutableReplacer createMutable();
}
