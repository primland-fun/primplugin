package ru.primland.plugin.modules.owner_check;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.modules.manager.Module;
import ru.primland.plugin.modules.manager.ModuleInfo;
import ru.primland.plugin.modules.manager.ModuleManager;
import ru.primland.plugin.utils.Utils;

@ModuleInfo(name="custom-items", config="owner_check", description="Защита от подбора кастомных вещей чужаками")
public class CustomItem extends Module implements Listener {
    private static Config config;
    /**
     * Загрузить (включить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(@NotNull PrimPlugin plugin) {
        config = getConfig();
        if(config == null)
            ModuleManager.disable("custom-items");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Отгрузить (выключить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {
        EntityPickupItemEvent.getHandlerList().unregister(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(@NotNull EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        CompoundTag tag = nmsItem.getTag();
        if(tag == null || !tag.contains("owner"))
            return;

        String owner = tag.getString("owner");
        LivingEntity entity = event.getEntity();
        if(owner.equals(entity.getName()))
            return;

        if(entity.hasPermission("primplugin.pickup_custom_items")) {
            Player player = Bukkit.getPlayer(owner);
            if(player == null) return;

            PrimPlugin.send(player, Utils.parse(config.getString("notification.message"),
                    new Placeholder("admin", entity.getName())));

            Utils.playSound(player, config.getString("notification.sound", null));
            return;
        }

        ((Player) entity).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                Utils.parse(config.getString("error"), new Placeholder("owner", owner))));

        event.setCancelled(true);
    }
}
