package dev.oop778.blixx.api.tag;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxColor;
import dev.oop778.blixx.api.component.BlixxStyle;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.util.collection.ObjectArray;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

/** Tag processing pipeline. Tags return a processor that applies their effect during component building. */
public interface BlixxProcessor {
    /** Processors that operate on the tag stack during parsing. */
    interface Tree extends BlixxProcessor {
        /** Filters the current tag stack (e.g., reset tag removes all existing tags). */
        interface Filterer extends Tree {
            ObjectArray<BlixxTag.WithDefinedData<?>> filter(
                    Context context, ObjectArray<? extends BlixxTag.WithDefinedData<?>> tags);
        }
    }

    /** Processors that operate during component building. */
    interface Component extends BlixxProcessor {
        /** Applies style modifications (color, decoration, click/hover events). */
        interface Decorator<T> extends Component {
            void decorate(@NonNull ComponentContext context);
        }

        /** Transforms node content (e.g., gradient applies per-character coloring). */
        interface Visitor<T> extends Component {
            void visit(@NonNull ComponentContext context);
        }

        @Data
        class ComponentContext {
            private final Blixx blixx;
            private Object data;
            private BlixxTag.WithDefinedData<?> tag;
            private BlixxNode node;
            private BlixxStyle style;
            private String content;

            // Callback for gradient visitor to append per-character colored text
            private GradientCharacterAppender gradientCharacterAppender;

            public ComponentContext(Blixx blixx) {
                this.blixx = blixx;
            }

            public <T> T getData() {
                return (T) this.data;
            }

            public void appendGradientCharacter(char character, @Nullable BlixxColor color) {
                if (this.gradientCharacterAppender != null) {
                    this.gradientCharacterAppender.append(character, color);
                }
            }
        }

        @FunctionalInterface
        interface GradientCharacterAppender {
            void append(char character, @Nullable BlixxColor color);
        }
    }

    @SuperBuilder
    @Getter
    class Context {
        private Blixx blixx;

        @Setter
        private BlixxNode node;
    }

    @SuperBuilder
    class ParserContext extends Context {}
}
