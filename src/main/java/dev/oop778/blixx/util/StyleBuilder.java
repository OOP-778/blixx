package dev.oop778.blixx.util;

import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class StyleBuilder implements Style.Builder {
    private Map<TextDecoration, TextDecoration.State> decorations;
    private @Nullable Key font;
    private @Nullable TextColor color;
    private @Nullable ClickEvent clickEvent;
    private @Nullable HoverEvent<?> hoverEvent;
    private @Nullable String insertion;
    private static final Map<TextDecoration, TextDecoration.State> EMPTY_DECORATIONS = Collections.emptyMap();

    private static final MethodHandle CREATE_STYLE_HANDLE;

    static {
        try {
            final Class<?> styleClazz = Class.forName("net.kyori.adventure.text.format.StyleImpl");
            final Constructor<?> declaredConstructor = styleClazz.getDeclaredConstructors()[0];
            declaredConstructor.setAccessible(true);

            CREATE_STYLE_HANDLE = MethodHandles.publicLookup().unreflectConstructor(declaredConstructor);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public StyleBuilder() {
        this.decorations = EMPTY_DECORATIONS;
    }

    static boolean nothingToMerge(final @NotNull Style mergeFrom, final Style.Merge.@NotNull Strategy strategy, final @NotNull Set<Style.Merge> merges) {
        if (strategy == Style.Merge.Strategy.NEVER) {
            return true;
        }
        if (mergeFrom.isEmpty()) {
            return true;
        }
        return merges.isEmpty();
    }

    @Override
    public Style.@NotNull Builder font(@Nullable Key font) {
        this.font = font;
        return this;
    }

    @Override
    public Style.@NotNull Builder color(@Nullable TextColor color) {
        this.color = color;
        return this;
    }

    @Override
    public Style.@NotNull Builder colorIfAbsent(@Nullable TextColor color) {
        if (this.color == null) {
            this.color = color;
        }

        return this;
    }

    @Override
    public Style.@NotNull Builder decoration(@NotNull TextDecoration decoration, TextDecoration.@NotNull State state) {
        if (this.decorations == EMPTY_DECORATIONS) {
            this.decorations = new EnumMap<>(TextDecoration.class);
        }

        this.decorations.put(decoration, state);
        return this;
    }

    @Override
    public Style.@NotNull Builder decorationIfAbsent(@NotNull TextDecoration decoration, TextDecoration.@NotNull State state) {
        if (this.decorations == EMPTY_DECORATIONS) {
            this.decorations = new EnumMap<>(TextDecoration.class);
        }

        this.decorations.putIfAbsent(decoration, state);
        return this;
    }

    @Override
    public Style.@NotNull Builder clickEvent(@Nullable ClickEvent event) {
        this.clickEvent = event;
        return this;
    }

    @Override
    public Style.@NotNull Builder hoverEvent(@Nullable HoverEventSource<?> source) {
        this.hoverEvent = HoverEventSource.unbox(source);
        return this;
    }

    @Override
    public Style.@NotNull Builder insertion(@Nullable String insertion) {
        this.insertion = insertion;
        return this;
    }

    @Override
    public @NotNull Style.Builder merge(final @NotNull Style that, final Style.Merge.@NotNull Strategy strategy, final @NotNull Set<Style.Merge> merges) {
        requireNonNull(that, "style");
        requireNonNull(strategy, "strategy");
        requireNonNull(merges, "merges");

        if (nothingToMerge(that, strategy, merges)) {
            return this;
        }

        if (merges.contains(Style.Merge.COLOR)) {
            final TextColor color = that.color();
            if (color != null) {
                if (strategy == Style.Merge.Strategy.ALWAYS || (strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.color == null)) {
                    this.color(color);
                }
            }
        }

        if (merges.contains(Style.Merge.DECORATIONS)) {
            for (final TextDecoration decoration : TextDecoration.values()) {
                final TextDecoration.State state = that.decoration(decoration);
                if (state != TextDecoration.State.NOT_SET) {
                    if (strategy == Style.Merge.Strategy.ALWAYS) {
                        this.decoration(decoration, state);
                    } else if (strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET) {
                        this.decorationIfAbsent(decoration, state);
                    }
                }
            }
        }

        if (merges.contains(Style.Merge.EVENTS)) {
            final ClickEvent clickEvent = that.clickEvent();
            if (clickEvent != null) {
                if (strategy == Style.Merge.Strategy.ALWAYS || (strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.clickEvent == null)) {
                    this.clickEvent(clickEvent);
                }
            }

            final HoverEvent<?> hoverEvent = that.hoverEvent();
            if (hoverEvent != null) {
                if (strategy == Style.Merge.Strategy.ALWAYS || (strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.hoverEvent == null)) {
                    this.hoverEvent(hoverEvent);
                }
            }
        }

        if (merges.contains(Style.Merge.INSERTION)) {
            final String insertion = that.insertion();
            if (insertion != null) {
                if (strategy == Style.Merge.Strategy.ALWAYS || (strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.insertion == null)) {
                    this.insertion(insertion);
                }
            }
        }

        if (merges.contains(Style.Merge.FONT)) {
            final Key font = that.font();
            if (font != null) {
                if (strategy == Style.Merge.Strategy.ALWAYS || (strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.font == null)) {
                    this.font(font);
                }
            }
        }

        return this;
    }

    @Override
    @SneakyThrows
    public @NotNull Style build() {
        if (this.isEmpty()) {
            return Style.empty();
        }


        return (Style) CREATE_STYLE_HANDLE.invoke(this.font, this.color, this.decorations, this.clickEvent, this.hoverEvent, this.insertion);
    }

    private boolean isEmpty() {
        return this.color == null
                && this.decorations == EMPTY_DECORATIONS
                && this.clickEvent == null
                && this.hoverEvent == null
                && this.insertion == null
                && this.font == null;
    }
}
