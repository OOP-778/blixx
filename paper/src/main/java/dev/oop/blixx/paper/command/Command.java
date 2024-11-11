package dev.oop.blixx.paper.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Command {
    void handle(CommandSender sender, CommandArgs arguments);

    default List<String> handleTabComplete(CommandSender sender, CommandArgs arguments) {
        return List.of();
    }
}
