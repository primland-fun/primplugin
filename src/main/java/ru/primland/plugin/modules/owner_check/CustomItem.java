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
import ru.primland.plugin.utils.Utils;

public class CustomItem implements IPluginModule, Listener {
    private boolean enabled;

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

        Config config = PrimPlugin.getInstance().getManager().getModuleConfig(getName());
        if(entity.hasPermission("primplugin.pickupCustomWeapons")) {
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

    /**
     * Получает и возвращает название данного модуля
     * @return Название модуля
     */
    @Override
    public String getName() {
        return "custom-weapons";
    }

    /**
     * Получает и возвращает название конфигурации данного модуля
     *
     * @return Название модуля
     */
    @Override
    public String getConfigName() {
        return "owner_check.yml";
    }

    /**
     * Получает и возвращает описание этого модуля
     * @return Описание модуля
     */
    @Override
    public String getDescription() {
        return "Защита от подбора кастомных вещей не их владельцами";
    }

    /**
     * Включает данный модуль
     * @param plugin Объект PrimPlugin
     */
    @Override
    public void enable(@NotNull PrimPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        enabled = true;
    }

    /**
     * Выключает этот модуль
     * @param plugin Объект PrimPlugin
     */
    @Override
    public void disable(PrimPlugin plugin) {
        EntityPickupItemEvent.getHandlerList().unregister(this);
        enabled = false;
    }

    /**
     * Включён ли модуль
     * @return Ответ на данный выше вопрос
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Немного информации о модуле плагина и его состоянии
     * @return Информация о модуле
     */
    @Override
    public String information() {
        return null;
    }
}
