package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.adventure.BlixxNodeAdventureImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.stream.Collector;

public interface BlixxComponent extends ComponentLike {
    static BlixxComponent wrap(Component component) {
        if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            component = component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }

        return new BlixxComponentImpl(new BlixxNodeAdventureImpl(null, component.colorIfAbsent(NamedTextColor.WHITE)));
    }

    static BlixxComponent empty() {
        return wrap(Component.empty());
    }

    static Collector<? super BlixxComponent, ?, BlixxComponent> joiningCollector(BlixxComponent delimiter, boolean includeAtTheEnd) {
        return new BlixxComponentCollector(delimiter, includeAtTheEnd);
    }

    static BlixxComponent joinWithNewLine(Iterable<? extends BlixxComponent> components) {
        return join(components, BlixxComponent.newLine());
    }

    static BlixxComponent newLine() {
        return BlixxComponent.wrap(Component.newline());
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

        return result == null ? BlixxComponent.wrap(Component.empty()) : result;
    }

    BlixxComponent replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, @Nullable PlaceholderContext context);

    BlixxComponent copy();

    @CheckReturnValue
    BlixxComponent append(@NonNull BlixxComponent... component);

    BlixxComponent append(@NonNull Component textComponent);

    BlixxComponent append(@NonNull Iterable<BlixxComponent> components);

    BlixxNode getNode();

    Component asComponent(Style defaultStyle);

    default BlixxComponent replace(Iterable<? extends BlixxPlaceholder<?>> placeholders) {
        return this.replace(placeholders, null);
    }
}
