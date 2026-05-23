package dev.oop778.blixx.api.component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/** Mutable style container holding color, decorations, click/hover events, font, and insertion text. */
@Getter
@Setter
public class BlixxStyle {
    private static final Set<BlixxDecoration> EMPTY_DECORATIONS = Collections.emptySet();

    private @Nullable BlixxColor color;
    private Set<BlixxDecoration> decorations;
    private @Nullable BlixxClickEvent clickEvent;
    private @Nullable BlixxHoverEvent hoverEvent;
    private @Nullable String font;
    private @Nullable String insertion;

    public BlixxStyle() {
        this.decorations = EMPTY_DECORATIONS;
    }

    public BlixxStyle color(@Nullable BlixxColor color) {
        this.color = color;
        return this;
    }

    public BlixxStyle colorIfAbsent(@Nullable BlixxColor color) {
        if (this.color == null) {
            this.color = color;
        }
        return this;
    }

    public BlixxStyle decorate(BlixxDecoration decoration) {
        if (this.decorations == EMPTY_DECORATIONS) {
            this.decorations = new HashSet<>();
        }
        this.decorations.add(decoration);
        return this;
    }

    public BlixxStyle clickEvent(@Nullable BlixxClickEvent event) {
        this.clickEvent = event;
        return this;
    }

    public BlixxStyle hoverEvent(@Nullable BlixxHoverEvent event) {
        this.hoverEvent = event;
        return this;
    }

    public BlixxStyle font(@Nullable String font) {
        this.font = font;
        return this;
    }

    public BlixxStyle insertion(@Nullable String insertion) {
        this.insertion = insertion;
        return this;
    }

    public boolean isEmpty() {
        return this.color == null
                && this.decorations == EMPTY_DECORATIONS
                && this.clickEvent == null
                && this.hoverEvent == null
                && this.font == null
                && this.insertion == null;
    }
}
