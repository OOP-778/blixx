package dev.oop778.blixx.hytale;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class ParseCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> inputArg;
    private final Blixx blixx;

    public ParseCommand(Blixx blixx) {
        super("blixx", "Parses input through Blixx and sends back the formatted message");
        this.blixx = blixx;
        this.inputArg = this.withRequiredArg("input", "The text to parse", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        final String input = this.inputArg.get(context);
        final BlixxComponent component = this.blixx.parseComponent(input);
        final Message message = HytaleNodeBuilder.build((BlixxNodeInternal) component.getNode());
        context.sendMessage(message);
        return CompletableFuture.completedFuture(null);
    }
}
