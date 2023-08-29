package ru.primland.plugin.modules.head_drop;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import io.github.stngularity.epsilon.engine.placeholders.TimePlaceholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.modules.manager.Module;
import ru.primland.plugin.modules.manager.ModuleInfo;
import ru.primland.plugin.modules.manager.ModuleManager;
import ru.primland.plugin.utils.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name="heads", config="heads", description="Выпадение голов с игроков", databaseRequired=true)
public class HeadDrop extends Module implements Listener {
    public static Config config;

    /**
     * Загрузить (включить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(@NotNull PrimPlugin plugin) {
        config = getConfig();
        if(config == null)
            ModuleManager.disable("heads");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Отгрузить (выключить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {
        PlayerDeathEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent event) {
        int chance = getChanceForPlayer(event.getEntity());
        if(Utils.randomInt(1, 100) > chance)
            return;

        event.getDrops().add(getPlayerHead(event.getEntity(), chance));
    }

    /**
     * Получить шанс выпадения головы для игрока
     *
     * @param player Объект игрока
     * @return Шанс от 1 до 100 процентов
     */
    public static int getChanceForPlayer(@NotNull Player player) {
        int reputation = PrimPlugin.driver.playerExists(player.getName()) ? PrimPlugin.driver
                .getPlayer(player.getName()).getReputation().getValue() : 0;

        int base = config.getInteger("chances.default", 100);

        String formula = config.getString("chances.formula", "$base-($rating*0.7)")
                .replace("$base", Integer.toString(base))
                .replace("$rating", Integer.toString(reputation));

        int chance = Utils.evalMathString(formula).intValue();
        return Math.min((chance < 1 ? base : chance), 100);
    }

    /**
     * Получить объект головы указанного игрока
     *
     * @param player Объект игрока
     * @param chance Шанс выпадения его головы
     * @return {@link ItemStack}
     */
    public static @NotNull ItemStack getPlayerHead(@NotNull Player player, int chance) {
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new TimePlaceholder(Utils.normalize(LocalDateTime.now())));
        placeholders.add(new Placeholder("player", player.getName()));
        placeholders.add(new Placeholder("chance", chance));

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if(meta != null) {
            meta.setOwnerProfile(player.getPlayerProfile());
            meta.setDisplayName(Utils.parse(config.getString("drop.displayName"), placeholders));

            List<String> lore = new ArrayList<>();
            config.getStringList("drop.lore").forEach(line -> lore.add(Utils.parse(line, placeholders)));
            meta.setLore(lore);

            head.setItemMeta(meta);
        }

        return head;
    }
}
