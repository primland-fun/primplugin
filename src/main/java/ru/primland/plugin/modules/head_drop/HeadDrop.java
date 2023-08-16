package ru.primland.plugin.modules.head_drop;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import io.github.stngularity.epsilon.engine.placeholders.TimePlaceholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.modules.IPluginModule;
import ru.primland.plugin.utils.Utils;
import ru.primland.plugin.utils.database.MySQLDriver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HeadDrop implements IPluginModule, Listener {
    private boolean enabled;

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent event) {
        PrimPlugin plugin = PrimPlugin.getInstance();
        Config config = plugin.getManager().getModuleConfig(getName());

        Player dead = event.getEntity();
        if(!(dead.getLastDamageCause() instanceof EntityDamageByEntityEvent damage))
            return;

        if(!(damage.getDamager() instanceof Player killer))
            return;

        int chance = getChanceForPlayer(config, dead);
        if(Utils.randomInt(1, 100) > chance)
            return;

        Utils.playSound(killer, config.getString("drop.sound"));
        event.getDrops().add(getPlayerHead(config, dead, chance));
    }

    private static @NotNull String getFormula(@NotNull Config config, @NotNull Integer base, @NotNull Integer rating) {
        return config.getString("chances.formula", "$base-($rating*0.7)")
                .replace("$base", base.toString())
                .replace("$rating", rating.toString());
    }

    public static int getChanceForPlayer(@NotNull Config config, @NotNull Player player) {
        MySQLDriver driver = PrimPlugin.getInstance().getDriver();
        int reputation = driver.playerExists(player.getName()) ? driver.getReputation(player.getName()) : 0;
        int base = config.getInteger("chances.default", 100);

        String formula = getFormula(config, base, reputation);
        int chance = Utils.evalMathString(formula).intValue();
        return Math.min((chance < 1 ? base : chance), 100);
    }

    public static @Nullable ItemStack getPlayerHead(Config config, Player player, int chance) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if(meta == null) return null;

        IPlaceholder[] placeholders = {new TimePlaceholder(LocalDateTime.now()),
            new Placeholder("player", player.getName()), new Placeholder("chance", chance)};

        meta.setOwnerProfile(player.getPlayerProfile());
        meta.setDisplayName(Utils.parse(config.getString("drop.displayName"), placeholders));

        List<String> lore = new ArrayList<>();
        config.getStringList("drop.lore").forEach(line -> lore.add(Utils.parse(line, placeholders)));
        meta.setLore(lore);

        head.setItemMeta(meta);
        return head;
    }

    /**
     * Получает и возвращает название данного модуля
     * @return Название модуля
     */
    @Override
    public String getName() {
        return "heads";
    }

    /**
     * Получает и возвращает название конфигурации данного модуля
     * @return Название модуля
     */
    @Override
    public String getConfigName() {
        return "head_drop.yml";
    }

    /**
     * Получает и возвращает описание этого модуля
     * @return Описание модуля
     */
    @Override
    public String getDescription() {
        return "Выпадение голов с игроков";
    }

    /**
     * Включает данный модуль
     * @param plugin Объект PrimPlugin
     */
    @Override
    public void enable(@NotNull PrimPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getManager().registerCommandFor(getName(), new GetCommand(
                PrimPlugin.getInstance().getManager().getModuleConfig(getName())));

        enabled = true;
    }

    /**
     * Выключает этот модуль
     * @param plugin Объект PrimPlugin
     */
    @Override
    public void disable(PrimPlugin plugin) {
        PlayerDeathEvent.getHandlerList().unregister(this);
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
