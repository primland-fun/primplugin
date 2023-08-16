package ru.primland.plugin.modules.head_drop;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.plugin.IPluginCommand;
import ru.primland.plugin.utils.Utils;

import java.util.List;

public class GetCommand implements IPluginCommand {
    private final Config config;

    public GetCommand(Config config) {
        this.config = config;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull List<String> args) {
        if(args.size() == 0) {
            PrimPlugin.send(sender, Utils.parse("command.errors.specifyPlayer"));
            return;
        }

        Player player = Bukkit.getPlayer(args.get(0));
        if(player == null) {
            PrimPlugin.send(sender, Utils.parse("command.errors.playerNotFound",
                    new Placeholder("player", args.get(0))));
            return;
        }

        ItemStack item = HeadDrop.getPlayerHead(config, player, HeadDrop.getChanceForPlayer(config, player));
        ((Player) sender).getInventory().addItem(item);

        PrimPlugin.send(sender, Utils.parse(config.getString("command.doneMessage"),
                new Placeholder("player", player.getName())));
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if(Math.max(args.length-1, 0) == 1)
            return PrimPlugin.getOnlinePlayersNames();

        return null;
    }

    @Override
    public String getName() {
        return "get";
    }

    @Override
    public String getDescription() {
        return "Выдаёт голову указанного игрока";
    }

    @Override
    public List<String> getRequiredPermissions() {
        return List.of("primplugin.commands.headDrop.getHead");
    }

    @Override
    public String getUsage() {
        return "{игрока}";
    }
}
