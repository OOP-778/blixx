package dev.oop.blixx.paper.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

public interface SimpleCommands {

    static void registerCommand(UnaryOperator<CommandData.CommandDataBuilder> commandBuilder) {
        final CommandData commandData = commandBuilder.apply(CommandData.builder()).build();
        final org.bukkit.command.Command bukkitCommand = Helper.createBukkitCommand(commandData);

        Helper.COMMAND_MAP.register("packetwords", bukkitCommand);
        ((CraftServer) Bukkit.getServer()).syncCommands();
    }

    @Builder
    @Getter
    public static class CommandData {
        private Command command;

        @Singular
        private List<CommandCondition> conditions;

        @Singular
        private List<String> aliases;

        @Builder.Default
        private boolean async = true;
    }

    class Helper {
        protected static final CommandMap COMMAND_MAP;
        protected static final Map<String, org.bukkit.command.Command> KNOWN_COMMANDS;

        static {
            try {
                final Method getCommandMap = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
                getCommandMap.setAccessible(true);
                COMMAND_MAP = (CommandMap) getCommandMap.invoke(Bukkit.getServer());

                final Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommands.setAccessible(true);
                KNOWN_COMMANDS = (Map<String, org.bukkit.command.Command>) knownCommands.get(COMMAND_MAP);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        protected static org.bukkit.command.Command createBukkitCommand(CommandData data) {
            final List<String> aliases = data.aliases.size() == 1 ? List.of() : data.aliases.subList(1, data.aliases.size());

            return new org.bukkit.command.Command(data.aliases.get(0), "", "", aliases) {
                @Override
                public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                    CompletableFuture.runAsync(() -> {
                        final CommandArgs arguments = new CommandArgs(args);

                        for (final CommandCondition condition : data.getConditions()) {
                            if (!condition.test(sender, arguments)) {
                                sender.sendMessage(Component.text("No permission", NamedTextColor.RED));
                                return;
                            }
                        }

                        try {
                            data.getCommand().handle(sender, arguments);
                        } catch (Throwable throwable) {
                            new IllegalStateException("failed to execute command", throwable).printStackTrace();
                        }
                    });

                    return true;
                }

                @Override
                public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                    return data.getCommand().handleTabComplete(sender, new CommandArgs(args));
                }
            };
        }
    }
}
