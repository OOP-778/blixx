package dev.oop778.blixx.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.oop778.blixx.api.Blixx;
import javax.annotation.Nonnull;

public class BlixxHytalePlugin extends JavaPlugin {
    private final Blixx blixx = Blixx.builder()
            .withStandardParserConfig(config -> config.withPlaceholderFormat('{', '}'))
            .withStandardPlaceholderConfig()
            .withPlatform(HytaleBlixx.INSTANCE)
            .build();

    public BlixxHytalePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new ParseCommand(this.blixx));
    }
}
