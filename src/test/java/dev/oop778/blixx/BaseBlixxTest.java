package dev.oop778.blixx;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.tag.BlixxTags;

public class BaseBlixxTest {
    protected static final Blixx BLIXX = Blixx.builder()
            .withStandardParserConfig((configurator) -> configurator
                    .withTags(BlixxTags.DEFAULT_TAGS)
                    .withPlaceholderFormat('%', '%')
                    .withPlaceholderFormat('{', '}')
            )
            .withStandardPlaceholderConfig()
            .build();
}
