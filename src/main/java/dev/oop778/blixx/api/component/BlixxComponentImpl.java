package dev.oop778.blixx.api.component;

import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
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
    public BlixxComponent replace(List<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        final BlixxNodeImpl copy = this.node.copy();
        copy.replace(placeholders, context);
        return new BlixxComponentImpl(copy);
    }

    @Override
    public @NotNull Component asComponent() {
        return this.node.build();
    }

    public BlixxComponent copy() {
        return new BlixxComponentImpl(this.node.copy());
    }
}
