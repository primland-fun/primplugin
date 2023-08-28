package ru.primland.plugin.modules.a2border;

import com.google.common.util.concurrent.AtomicDouble;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
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
import ru.primland.plugin.commands.plugin.IPluginCommand;
import ru.primland.plugin.utils.Utils;

import java.util.List;

public class Achievements2Border implements Listener, IPluginModule {
    private boolean enabled;

    @EventHandler
    public void playerGetAchievement(@NotNull PlayerAdvancementDoneEvent event) {
        Config config = PrimPlugin.getInstance().getManager().getModuleConfig(getName());
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

    /**
     * Получает и возвращает название данного модуля
     * @return Название модуля
     */
    @Override
    public String getName() {
        return "a2border";
    }

    /**
     * Получает и возвращает название конфигурации данного модуля
     * @return Название модуля
     */
    @Override
    public String getConfigName() {
        return "a2border.yml";
    }

    /**
     * Получает и возвращает описание этого модуля
     * @return Описание модуля
     */
    @Override
    public String getDescription() {
        return "Конвертирует все достижения в размер границ";
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
        PlayerAdvancementDoneEvent.getHandlerList().unregister(this);
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
    public String information() {
        Config config = PrimPlugin.getInstance().getManager().getModuleConfig(getName());

        StringBuilder information = new StringBuilder(" \n");
        information.append(config.getString("infoMessage.header"));
        information.append("\n");

        Bukkit.getWorlds().forEach(world -> {
            long borderSize = Math.round(world.getWorldBorder().getSize() / 4);
            String size = borderSize + "x" + borderSize;
            information.append(Utils.parse(config.getString("infoMessage.content.world"),
                    new Placeholder("world", world.getName()),
                    new Placeholder("size", size)));

            information.append("\n");
        });

        double dSize = config.getDouble("defaultSize", 2.0);

        information.append(Utils.parse(config.getString("infoMessage.content.achievement"),
                new Placeholder("type", "задачи"),
                new Placeholder("add", dSize)));

        information.append("\n");
        information.append(Utils.parse(config.getString("infoMessage.content.achievement"),
                new Placeholder("type", "челленджи"),
                new Placeholder("add", dSize*config.getDouble("multipliers.challenge", 3.0))));

        information.append("\n");
        information.append(Utils.parse(config.getString("infoMessage.content.achievement"),
                new Placeholder("type", "цели"),
                new Placeholder("add", dSize*config.getDouble("multipliers.goal", 6.0))));

        information.append("\n ");
        return information.toString();
    }

    /**
     * Получает список команда модуля данного плагина
     * @return Список с командами
     */
    public List<IPluginCommand> getCommands() {
        return List.of();
    }
}
