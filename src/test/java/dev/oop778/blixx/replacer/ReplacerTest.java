package dev.oop778.blixx.replacer;

import dev.oop778.blixx.BaseBlixxTest;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import dev.oop778.blixx.api.replacer.processor.ReplacerProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class ReplacerTest extends BaseBlixxTest {
    public static void main(String[] args) {
        final Replacer hello = Replacer.createImmutable()
                .withPlaceholderAtEnd()
                .literal()
                .withKey("hello")
                .withValue(1)
                .build();

        final List<Component> hh = Replacer.createImmutable()
                .withPlaceholder(PlaceholderHolder.Where.END, BlixxPlaceholder.literal("hh", 1))
                .accept(Component.text("hello <hello>", NamedTextColor.RED))
                .postReplacing(ReplacerProcessor.adventureNewLineFlattener())
                .complete();

        final MutableReplacer mutable = Replacer.createMutable(BlixxPlaceholder.literal("gg", 1));

        hello
                .accept(Component.text("hello <hello>", NamedTextColor.RED))
                .preReplacing(ReplacerProcessor.adventureNewLineFlattener())
                .complete();

        final BlixxComponent complete = hello.accept(BLIXX.parseComponent("<red>Hello <hello>")).complete();
        System.out.println(LegacyComponentSerializer.legacyAmpersand().serialize(complete.asComponent()));
    }

    public abstract static class ItemBuilder<I, B extends ItemBuilder<I, B>> {
        @Nullable
        protected BlixxComponent displayName;
        @Nullable
        protected BlixxComponent lore;
        @Nullable
        protected Function<I, I> postBuild;

        public ItemBuilder(ItemBuilder<?, ?> itemBuilder) {
            this.displayName = itemBuilder.displayName;
            this.lore = itemBuilder.lore;
        }

        protected abstract I buildItem();

        protected abstract B copy();

        public B withDisplayName(BlixxComponent component) {
            return this.withCopyAction(builder -> builder.displayName = component);
        }

        public B withLore(BlixxComponent component) {
            return this.withCopyAction(builder -> builder.lore = component);
        }

        public B replaceLore(Replacer replacer) {
            if (this.lore == null) {
                return (B) this;
            }

            return this.withCopyAction((builder) -> {
                builder.lore = replacer.accept(this.lore).complete();
            });
        }

        public B withPostBuildTransformer(UnaryOperator<I> postBuild) {
            return this.withCopyAction((builder) -> {
                if (this.postBuild == null) {
                    builder.postBuild = postBuild;
                } else {
                    builder.postBuild = this.postBuild.andThen(postBuild);
                }
            });
        }

        public I build() {
            I item = this.buildItem();
            if (this.postBuild != null) {
                item = this.postBuild.apply(item);
            }

            return item;
        }

        protected B withCopyAction(Consumer<B> action) {
            final B copy = this.copy();
            action.accept(copy);

            return copy;
        }
    }
}
