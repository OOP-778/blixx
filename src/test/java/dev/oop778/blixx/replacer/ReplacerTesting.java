package dev.oop778.blixx.replacer;

import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.replacer.processor.ReplacerProcessor;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.ArrayList;
import java.util.List;

public class ReplacerTesting {
    /**
     * replacer.replace(object)
     * .preProcess()
     * .postProcess()
     * .complete()
     */
    public static void main(String[] args) {
        final List<BlixxComponent> list = new ArrayList<>();
        final List<Component> complete = replacer(list)
                .preReplacing(ReplacerProcessor.newLineJoiner())
                .postReplacing(ComponentLike::asComponent)
                .postReplacing(ReplacerProcessor.adventureNewLineFlattener())
                .complete();
    }

    public static <T> ReplacerWork<T> replacer(T object) {
        return null;
    }

    public interface ReplacerWork<T> {
        <OUT> ReplacerWork<OUT> preReplacing(@NonNull ReplacerProcessor<T, OUT> processor);
        <OUT> ReplacerWork<OUT> postReplacing(@NonNull ReplacerProcessor<T, OUT> processor);

        T complete();
    }
}
