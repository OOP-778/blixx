package dev.oop778.blixx.adventure.component;

import dev.oop778.blixx.adventure.AdventureNodeBuilder;
import dev.oop778.blixx.adventure.node.BlixxNodeAdventureImpl;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

public class AdventureBlixxComponent {
    public static Component asComponent(BlixxComponent component) {
        return asComponent(component, null);
    }

    public static Component asComponent(BlixxComponent component, @Nullable Style defaultStyle) {
        return AdventureNodeBuilder.build((BlixxNodeInternal) component.getNode(), defaultStyle);
    }

    public static BlixxComponent wrap(Component component) {
        if (component == Component.empty()) {
            return BlixxComponent.empty();
        }

        if (component == Component.newline()) {
            return BlixxComponent.newLine();
        }

        if (component == Component.space()) {
            return BlixxComponent.space();
        }

        return BlixxComponent.fromNode(new BlixxNodeAdventureImpl(component));
    }
}
