package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.BlixxProvider;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.spi.BlixxFactoryHolder;
import java.util.Iterator;
import java.util.stream.Collector;
import lombok.NonNull;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

public interface BlixxComponent {
    static BlixxComponent fromNode(BlixxNode node) {
        return BlixxFactoryHolder.get().createComponent(node);
    }

    static BlixxComponent space() {
        return BlixxFactoryHolder.get().createSpaceComponent();
    }

    static BlixxComponent empty() {
        return BlixxFactoryHolder.get().createEmptyComponent();
    }

    static BlixxComponent parse(String input) {
        return BlixxProvider.get().parseComponent(input);
    }

    static BlixxComponent parse(Iterable<? extends CharSequence> input) {
        return parse(String.join("\n", input));
    }

    static Collector<? super BlixxComponent, ?, BlixxComponent> joiningCollector(
            BlixxComponent delimiter, boolean includeAtTheEnd) {
        return BlixxFactoryHolder.get().createComponentCollector(delimiter, includeAtTheEnd);
    }

    static BlixxComponent joinWithNewLine(Iterable<? extends BlixxComponent> components) {
        return join(components, BlixxComponent.newLine());
    }

    static BlixxComponent newLine() {
        return BlixxFactoryHolder.get().createNewLineComponent();
    }

    static BlixxComponent join(Iterable<? extends BlixxComponent> what, BlixxComponent with) {
        BlixxComponent result = null;
        final Iterator<? extends BlixxComponent> iterator = what.iterator();
        while (iterator.hasNext()) {
            result = result == null ? iterator.next().copy() : result.append(iterator.next());

            if (iterator.hasNext()) {
                result = result.append(with);
            }
        }

        return result == null ? BlixxComponent.empty() : result;
    }

    BlixxComponent replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, @Nullable PlaceholderContext context);

    BlixxComponent copy();

    @CheckReturnValue
    BlixxComponent append(@NonNull BlixxComponent... component);

    BlixxComponent append(@NonNull Iterable<BlixxComponent> components);

    BlixxNode getNode();

    default BlixxComponent replace(Iterable<? extends BlixxPlaceholder<?>> placeholders) {
        return this.replace(placeholders, null);
    }
}
