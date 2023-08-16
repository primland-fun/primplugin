package ru.primland.plugin.commands;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.Bukkit;
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
import java.util.Arrays;
import java.util.List;

public class KickCommand implements TabExecutor {
    private Config config;

    public KickCommand(Config config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        Config i18n = PrimPlugin.getInstance().getI18n();
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", sender.getName()));

        if(!sender.hasPermission("primplugin.commands.kick")) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("notEnoughRights"), placeholders));
            return true;
        }

        if(args.length == 0) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("specifyPlayerNotSelf"), placeholders));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        placeholders.add(new Placeholder("player", args[0]));
        if(player == null) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("playerNotFound"), placeholders));
            return true;
        }

        if(player.getName().equals(sender.getName())) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("selfSpecified"), placeholders));
            return true;
        }

        if(player.hasPermission("primplugin.commands.kick")) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.adminSpecified"), placeholders));
            return true;
        }

        placeholders.add(new Placeholder("reason", args.length < 2 ? config.getString("defaultReason")
                : String.join(" ", Arrays.copyOfRange(args, 1, args.length))));


        player.kickPlayer(Utils.parse(String.join("\n", config.getStringList("displayText")), placeholders));
        Bukkit.broadcastMessage(Utils.parse(String.join("\n", config.getStringList("message")), placeholders));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        if(Math.max(args.length-1, 0) == 1) {
            List<String> names = new ArrayList<>();
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                if(player.hasPermission("primplugin.commands.kick")) return;
                names.add(player.getName());
            });

            return names;
        }

        return List.of();
    }

    public void updateConfig(Config config) {
        this.config = config;
    }
}
