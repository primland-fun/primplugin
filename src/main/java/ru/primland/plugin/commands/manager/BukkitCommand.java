package ru.primland.plugin.commands.manager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.annotations.CommandInfo;

import java.util.List;

public class BukkitCommand extends Command {
    private final CommandInfo info;

    public BukkitCommand(ICommand command, @NotNull CommandInfo info) {
        super(info.name(), info.description(), CommandManager.getUsage(info), List.of(info.aliases()));
        this.info = info;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        if(!info.permission().isEmpty() && !sender.hasPermission(info.permission())) {
            PrimPlugin.send(PrimPlugin.i18n.getString("notEnoughRights"));
            return true;
        }



        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}