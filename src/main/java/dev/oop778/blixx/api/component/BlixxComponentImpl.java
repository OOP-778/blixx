package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.BlixxRootNodeImpl;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class BlixxComponentImpl implements BlixxComponent {
    private final BlixxRootNodeImpl node;

    @Override
    public BlixxComponent toMutable() {
        return null;
    }

    @Override
    public @NotNull Component asComponent() {
        return this.node.build();
    }
}
