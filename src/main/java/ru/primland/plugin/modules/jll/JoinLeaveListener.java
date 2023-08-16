package ru.primland.plugin.modules.jll;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import io.github.stngularity.epsilon.engine.placeholders.TimePlaceholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.WhitelistCommand;
import ru.primland.plugin.modules.cards.GiveBoxCommand;
import ru.primland.plugin.modules.recipes.CustomRecipes;
import ru.primland.plugin.utils.Utils;
import ru.primland.plugin.utils.database.ChatSettings;
import ru.primland.plugin.utils.database.MySQLDriver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class JoinLeaveListener implements Listener {
    private Config config;

    @EventHandler
    public void playerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        whitelist(player, event);

        MySQLDriver driver = PrimPlugin.getInstance().getDriver();
        if(!driver.isNotWorking() && !driver.playerExists(player.getName())) {
            driver.addPlayer(player);
            GiveBoxCommand.giveBox(player, 2, Config.load("modules/cards.yml"));
        }

        messages(player);
        recipes(player);

        event.setJoinMessage(Utils.parse(config.getString("joinMessage"),
                new Placeholder("player", player.getName())));
    }

    public void whitelist(@NotNull Player player, @NotNull PlayerJoinEvent event) {
        WhitelistCommand whitelist = PrimPlugin.getInstance().getWhitelistCommand();
        if(whitelist.whitelistContains(player))
            return;

        event.setJoinMessage(null);
        player.kickPlayer(whitelist.getReason());
    }

    private void messages(@NotNull Player player) {
        MySQLDriver driver = PrimPlugin.getInstance().getDriver();
        if(!driver.playerExists(player.getName()))
            return;

        ChatSettings chat = driver.getChatSettings(player.getName());
        if(chat.getMessages().size() > 0) {
            Config pmConfig = Config.load("commands/private_messages.yml");
            player.sendMessage(Utils.parse(pmConfig.getString("receiver.missedMessages.message"),
                    new Placeholder("count", chat.getMessages().size())));

            String sound = chat.getSound();
            if(sound != null)
                Utils.playSound(player, sound.equals("$DEFAULT") ? pmConfig.getString("receiver.sound", null) : sound);

            chat.getMessages().forEach(message -> {
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getTime()), TimeZone.getDefault().toZoneId());
                player.sendMessage(Utils.parse(pmConfig.getString("receiver.missedMessages.format"),
                        new TimePlaceholder(time), new Placeholder("sender", message.getSender()),
                        new Placeholder("receiver", player.getName()),
                        new Placeholder("message", message.getContent())));
            });

            driver.clearMessages(player.getName());
        }
    }

    private void recipes(@NotNull Player player) {
        CustomRecipes.getRegisteredRecipes().forEach(recipe -> {
            if(player.hasDiscoveredRecipe(recipe)) return;
            player.discoverRecipe(recipe);
        });
    }

    @EventHandler
    public void playerLeave(@NotNull PlayerQuitEvent event) {
        event.setQuitMessage(Utils.parse(config.getString("leaveMessage"),
                new Placeholder("player", event.getPlayer().getName())));
    }

    public void enable(@NotNull PrimPlugin plugin) {
        config = Config.load("modules/messages.yml");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void disable() {
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }
}
