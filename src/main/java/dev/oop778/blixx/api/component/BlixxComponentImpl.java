package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.BlixxNodeImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class BlixxComponentImpl implements BlixxComponent {
    private final BlixxNodeImpl node;

    @Override
    public BlixxComponent toMutable() {
        return null;
    }

    @Override
    public BlixxComponent replace(List<BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        this.node.replace(placeholders, context);
        return this;
    }

    @Override
    public @NotNull Component asComponent() {
        return this.node.build();
    }

    public BlixxComponent copy() {
        return new BlixxComponentImpl(this.node.copy());
    }
}
