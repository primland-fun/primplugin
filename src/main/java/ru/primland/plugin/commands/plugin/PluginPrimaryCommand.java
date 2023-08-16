package ru.primland.plugin.commands.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PluginPrimaryCommand implements TabExecutor {
    private final List<IPluginCommand> commands = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("primplugin.commands.primary")) {
            PrimPlugin.send(sender, "&cИзвините, но у Вас недостаточно прав для выполнения этой команды");
            return true;
        }

        if(args.length == 0)
            return onCommand(sender, command, label, List.of("help").toArray(new String[0]));

        for(IPluginCommand cmd : commands) {
            if(!cmd.getName().equals(args[0]))
                continue;

            for(String permission : cmd.getRequiredPermissions()) {
                if(sender.hasPermission(permission))
                    continue;

                PrimPlugin.send(sender, "&cИзвините, но у Вас недостаточно прав для выполнения этой команды");
                return true;
            }

            List<String> cArgs = new ArrayList<>(List.of(args));
            cArgs.remove(0);
            cmd.execute(sender, cArgs);
            return true;
        }

        PrimPlugin.send(sender, "&cИзвините, но данная команда не найдена");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        int argNumber = Math.max(args.length-1, 0);
        if(argNumber == 0) {
            List<String> names = new ArrayList<>();
            for(IPluginCommand cmd : commands) {
                if(hasNotPermissions(sender, cmd.getRequiredPermissions())) continue;
                names.add(cmd.getName());
            }

            return names;
        }

        for(IPluginCommand cmd : commands) {
            if(!cmd.getName().equals(args[0])) continue;
            return cmd.tabComplete(sender, args);
        }

        return null;
    }

    public static boolean hasNotPermissions(CommandSender sender, @NotNull List<String> permissions) {
        for(String permission : permissions) {
            if(sender.hasPermission(permission)) continue;
            return true;
        }

        return false;
    }

    public void addCommand(IPluginCommand command) {
        this.commands.add(command);
    }

    public void removeCommand(String name) {
        int index = -1;
        for(IPluginCommand cmd : this.commands) {
            if(name.equals(cmd.getName())) break;
            index++;
        }

        if(index >= 0 && Objects.equals(this.commands.get(index).getName(), name))
            this.commands.remove(index);
    }

    public List<IPluginCommand> getCommands() {
        return commands;
    }

    public boolean hasCommand(String commandName) {
        for(IPluginCommand command : commands) {
            if(!command.getName().equals(commandName)) continue;
            return true;
        }

        return false;
    }
}
