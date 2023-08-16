package ru.primland.plugin.commands;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class WhitelistCommand implements TabExecutor {
    private final Config config;

    public WhitelistCommand(Config config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 0) {
            PrimPlugin.send(sender, config.getString("errors.specifyAction"));
            return true;
        }

        String action = args[0];
        if(!Utils.equalsOne(action, "add", "remove", "list")) {
            PrimPlugin.send(sender, config.getString("errors.invalidAction"));
            return true;
        }

        if(args.length == 1 && !action.equals("list")) {
            PrimPlugin.send(sender, PrimPlugin.getInstance().getI18n().getString("specifyPlayer"));
            return true;
        }

        if(action.equals("list")) {
            String players = String.join(config.getString("messages.listSeparator", ", "),
                    config.getStringList("whitelist"));

            sender.sendMessage(Utils.parse(config.getString("messages.list"),
                    new Placeholder("count", config.getStringList("whitelist").size()),
                    new Placeholder("players", players)));

            return true;
        }

        String player = args[1];
        List<String> whitelist = config.getStringList("whitelist");
        if(action.equals("add") && whitelist.contains(player)) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.playerExistsInWhitelist"),
                    new Placeholder("player", player)));
            return true;
        }

        if(action.equals("add")) {
            whitelist.add(player);
            config.set("whitelist", whitelist);
            config.save();

            PrimPlugin.send(sender, Utils.parse(config.getString("messages.add"),
                    new Placeholder("player", player)));

            return true;
        }

        if(!whitelist.contains(player)) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.playerNotExistsInWhitelist"),
                    new Placeholder("player", player)));
            return true;
        }

        for(int i = 0; i < whitelist.size(); i++) {
            if(!whitelist.get(i).equals(player)) continue;
            whitelist.remove(i);
            break;
        }

        config.set("whitelist", whitelist);
        config.save();

        PrimPlugin.send(sender, Utils.parse(config.getString("messages.remove"),
                new Placeholder("player", player)));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        int argNumber = Math.max(args.length-1, 0);
        if(argNumber == 0) {
            List<String> output = new ArrayList<>();
            if(sender.hasPermission("primplugin.commands.whitelist.add"))
                output.add("add");

            if(sender.hasPermission("primplugin.commands.whitelist.remove"))
                output.add("remove");

            if(sender.hasPermission("primplugin.commands.whitelist.list"))
                output.add("list");

            return output;
        }

        if(argNumber == 1 && !args[0].equals("list"))
            return PrimPlugin.getOnlinePlayersNames();

        return List.of();
    }

    public boolean whitelistContains(@NotNull Player player) {
        return config.getStringList("whitelist").contains(player.getName());
    }

    public String getReason() {
        return Utils.parse(String.join("\n", config.getStringList("messages.kick")));
    }

    public void reload() {
        config.reload();
    }
}
