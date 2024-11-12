package dev.oop778.blixx.api.replacer.processor;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.util.adventure.AdventureComponentSplitter;
import dev.oop778.blixx.util.adventure.AdventureUtils;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

import java.util.List;

@FunctionalInterface
public interface ReplacerProcessor<IN, OUT> {
    static ReplacerProcessor<List<Component>, Component> adventureNewLineJoiner() {
        return (input) -> AdventureUtils.join(Component.newline(), input);
    }

    static ReplacerProcessor<Component, List<Component>> adventureNewLineFlattener() {
        return (input) -> AdventureComponentSplitter.split(Component.newline(), input);
    }

    @NonNull
    OUT accept(IN input);

    interface Context {
        Blixx getBlixx();
    }
}
