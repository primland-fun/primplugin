package ru.primland.plugin.commands;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.Utils;
import ru.primland.plugin.utils.database.ChatSettings;
import ru.primland.plugin.utils.database.Message;
import ru.primland.plugin.utils.database.MySQLDriver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivateMessage implements TabExecutor {
    private Config config;

    public PrivateMessage(Config config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        Config i18n = PrimPlugin.getInstance().getI18n();
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", sender.getName()));

        if(args.length == 0) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("specifyPlayerNotSelf"), placeholders));
            return true;
        }

        placeholders.add(new Placeholder("receiver", args[0]));
        Player receiver = Bukkit.getPlayer(args[0]);
        if(args[0].equals(sender.getName())) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("selfSpecified"), placeholders));
            return true;
        }

        if(args.length < 2) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.specifyMessage"), placeholders));
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        placeholders.add(new Placeholder("message", message));

        MySQLDriver driver = PrimPlugin.getInstance().getDriver();
        if(receiver == null && !driver.playerExists(args[0])) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("playerNotFound"), placeholders));
            return true;
        }

        sender.sendMessage(Utils.parse(config.getString("sender.message"), placeholders));
        if(!(sender instanceof ConsoleCommandSender))
            Utils.playSound((Player) sender, config.getString("sender.sound", null));

        ChatSettings chat = driver.getChatSettings(args[0]);
        if(receiver != null) {
            receiver.sendMessage(Utils.parse(config.getString("receiver.message"), placeholders));
            String sound = chat.getSound();
            if(sound != null)
                Utils.playSound(receiver, sound.equals("$DEFAULT") ? config.getString("receiver.sound", null) : sound);

            return true;
        }

        driver.addMessage(args[0], new Message(sender.getName(), message, LocalDateTime.now()));
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        if(Math.max(args.length-1, 0) == 1)
            return PrimPlugin.getOnlinePlayersNames();

        return null;
    }

    public void updateConfig(Config config) {
        this.config = config;
    }
}
