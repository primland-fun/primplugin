package ru.primland.plugin.modules.jll;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import io.github.stngularity.epsilon.engine.placeholders.TimePlaceholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.PrivateMessage;
import ru.primland.plugin.commands.whitelist.WhitelistCommand;
import ru.primland.plugin.database.data.Message;
import ru.primland.plugin.database.data.PrimPlayer;
import ru.primland.plugin.modules.cards.GiveBoxCommand;
import ru.primland.plugin.modules.recipes.CustomRecipes;
import ru.primland.plugin.utils.Utils;

import java.util.List;

public class JoinLeaveListener implements Listener {
    public void enable(@NotNull PrimPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void disable() {
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPreLogin(@NotNull AsyncPlayerPreLoginEvent event) {
        if(!WhitelistCommand.config.getBoolean("enabled", true))
            return;

        if(WhitelistCommand.whitelistContains(event.getName()))
            return;

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, WhitelistCommand.getReason());
    }

    @EventHandler
    public void playerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(PrimPlugin.driver.isWorking() && !PrimPlugin.driver.playerExists(player.getName())) {
            PrimPlugin.driver.addPlayer(player);
            GiveBoxCommand.giveBox(player, 2);
        }

        messages(player);
        recipes(player);

        event.setJoinMessage(Utils.parse(PrimPlugin.i18n.getString("joinMessage"),
                new Placeholder("player", player.getName())));
    }

    private void messages(@NotNull Player player) {
        if(!PrimPlugin.driver.playerExists(player.getName()))
            return;

        PrimPlayer primPlayer = PrimPlugin.driver.getPlayer(player.getName());
        List<Message> messages = primPlayer.searchUnreadMessages();
        if(messages.isEmpty())
            return;

        player.sendMessage(Utils.parse(PrivateMessage.config.getString("receiver.missedMessages.message"),
                new Placeholder("count", messages.size())));

        Utils.playSound(player, primPlayer.getChat().getSound());
        messages.forEach(message -> player.sendMessage(
                Utils.parse(PrivateMessage.config.getString("receiver.missedMessages.format"),
                new TimePlaceholder(message.getTime()),
                new Placeholder("sender", message.getSender()),
                new Placeholder("receiver", player.getName()),
                new Placeholder("message", message.getContent()))
        ));

        primPlayer.markAllMessagesAsRead();
    }

    private void recipes(@NotNull Player player) {
        CustomRecipes.registeredRecipes.forEach(recipe -> {
            if(player.hasDiscoveredRecipe(recipe)) return;
            player.discoverRecipe(recipe);
        });
    }

    @EventHandler
    public void playerLeave(@NotNull PlayerQuitEvent event) {
        event.setQuitMessage(Utils.parse(PrimPlugin.i18n.getString("leaveMessage"),
                new Placeholder("player", event.getPlayer().getName())));
    }
}
