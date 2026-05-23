package dev.oop778.blixx.adventure.replacer;

import dev.oop778.blixx.adventure.util.AdventureComponentSplitter;
import dev.oop778.blixx.adventure.util.AdventureUtils;
import dev.oop778.blixx.api.replacer.processor.ReplacerProcessor;
import java.util.List;
import net.kyori.adventure.text.Component;

public class AdventureReplacerProcessor {
    public static ReplacerProcessor<List<Component>, Component> newLineJoiner() {
        return (input) -> AdventureUtils.join(Component.newline(), input);
    }

    public static ReplacerProcessor<Component, List<Component>> newLineFlattener() {
        return (input) -> AdventureComponentSplitter.split(Component.newline(), input);
    }
}
