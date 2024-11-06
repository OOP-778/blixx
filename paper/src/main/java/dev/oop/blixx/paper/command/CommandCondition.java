package dev.oop.blixx.paper.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

@FunctionalInterface
public interface CommandCondition {
    boolean test(CommandSender sender, CommandArgs arguments);

    static CommandCondition isConsole() {
        return (sender, $) -> sender instanceof ConsoleCommandSender;
    }

    static CommandCondition isOp() {
        return (sender, $) -> sender.isOp();
    }

    static CommandCondition withPermission(String permission) {
        return (sender, $) -> sender.hasPermission(permission);
    }
}
