package ru.primland.plugin.modules.a2border;

import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.advancement.AdvancementDisplayType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.modules.manager.Module;
import ru.primland.plugin.modules.manager.ModuleInfo;

@ModuleInfo(name="a2border", config="a2border", description="Достижения = граница")
public class Achievements2Border extends Module implements Listener {
    /**
     * Загрузить (включить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(@NotNull PrimPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Отгрузить (выключить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {
        PlayerAdvancementDoneEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void playerGetAchievement(@NotNull PlayerAdvancementDoneEvent event) {
        Config config = getConfig();
        if(config == null)
            return;

        AtomicDouble size = new AtomicDouble(config.getDouble("defaultSize", 3.0));

        AdvancementDisplay advancement = event.getAdvancement().getDisplay();
        if(advancement == null)
            return;

        AdvancementDisplayType type = advancement.getType();
        if(type == AdvancementDisplayType.CHALLENGE)
            size.set(size.get()*config.getDouble("multipliers.challenge", 3.0));

        if(type == AdvancementDisplayType.GOAL)
            size.set(size.get()*config.getDouble("multipliers.goal", 6.0));

        WorldBorder border = Bukkit.getWorlds().get(0).getWorldBorder();  // Получаем верхний мир
        border.setSize(border.getSize() + size.get()*2);                  // 1 - 1/2 блока; 2 - 1 блок

        World world = Bukkit.getWorld("world_nether");
        if(world != null) {
            WorldBorder nBorder = world.getWorldBorder();
            nBorder.setSize(border.getSize() / 8);
        }
    }
}
