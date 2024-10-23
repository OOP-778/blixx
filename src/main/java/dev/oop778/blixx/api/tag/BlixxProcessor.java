package dev.oop778.blixx.api.tag;

import dev.oop778.blixx.api.Blixx;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.List;

public interface BlixxProcessor {
    interface Tree extends BlixxProcessor {
        interface Filterer extends Tree {
            void filter(Context context, List<? extends BlixxTag.WithDefinedData<?>> tags);
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
            private Style.Builder styleBuilder;
            private TextComponent.Builder componentBuilder;

            public <T> T getData() {
                return (T) data;
            }
        }
    }

    @SuperBuilder
    @Getter
    class Context {
        private Blixx blixx;
    }
}
