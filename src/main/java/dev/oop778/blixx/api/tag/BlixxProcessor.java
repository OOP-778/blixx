package dev.oop778.blixx.api.tag;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.ParsingContext;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.util.adventure.FastComponentBuilder;
import dev.oop778.blixx.util.collection.ObjectArray;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.text.format.Style;

public interface BlixxProcessor {
    interface Tree extends BlixxProcessor {
        interface Filterer extends Tree {
            ObjectArray<BlixxTag.WithDefinedData<?>> filter(Context context, ObjectArray<? extends BlixxTag.WithDefinedData<?>> tags);
        }
    }

    interface Component extends BlixxProcessor {
        interface Decorator<T> extends Component {
            void decorate(@NonNull ComponentContext context);
        }

        interface Visitor<T> extends Component {
            void visit(@NonNull ComponentContext context);
        }

        @SuperBuilder
        @Data
        @EqualsAndHashCode(callSuper = true)
        class ComponentContext extends Context {
            private Object data;
            private BlixxTag.WithDefinedData<?> tag;
            private Style.Builder styleBuilder;
            private FastComponentBuilder componentBuilder;

            public <T> T getData() {
                return (T) this.data;
            }
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
    class ParserContext extends Context {
        private ParsingContext parsingContext;

        public Object createKey() {
            return this.parsingContext.createNewKey();
        }
    }
}
