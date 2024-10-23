package dev.oop778.blixx.text.argument.hook;

import dev.oop778.blixx.text.argument.BaseArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MiniMessageArgumentQueue implements ArgumentQueue {
    private final BaseArgumentQueue queue;

    public MiniMessageArgumentQueue(BaseArgumentQueue queue) {this.queue = queue;}

    @Override
    public Tag.@NotNull Argument pop() {
        return new AdventureArgument(this.queue.pop());
    }

    @Override
    public Tag.@NotNull Argument popOr(@NotNull String errorMessage) {
        return new AdventureArgument(this.queue.pop());
    }

    @Override
    public Tag.@NotNull Argument popOr(@NotNull Supplier<String> errorMessage) {
        return new AdventureArgument(this.queue.pop());
    }

    @Override
    public Tag.@Nullable Argument peek() {
        return new AdventureArgument(this.queue.peek());
    }

    @Override
    public boolean hasNext() {
        return !this.queue.hasNext();
    }

    @Override
    public void reset() {
        this.queue.reset();
    }

    public static class AdventureArgument implements Tag.Argument {
        private final String value;

        public AdventureArgument(String value) {this.value = value;}

        @Override
        public @NotNull String value() {
            return this.value;
        }
    }
}
