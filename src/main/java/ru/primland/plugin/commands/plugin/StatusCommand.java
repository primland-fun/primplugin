package ru.primland.plugin.commands.plugin;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import io.github.stngularity.epsilon.engine.placeholders.TimePlaceholder;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.modules.manager.ModuleManager;
import ru.primland.plugin.utils.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@CommandInfo(
        name="status",
        description="Информация о состоянии плагина",
        parent="primplugin",
        permission="primplugin.commands.status"
)
public class StatusCommand extends Command {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {}

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {}

    /**
     * Выполнить команду с указанными данными
     *
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    @Override
    public @Nullable String execute(CommandContext ctx) {
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new TimePlaceholder("now", LocalDateTime.now()));

        StringBuilder output = new StringBuilder(Utils.parse(String.join("\n",
                PrimPlugin.i18n.getStringList("status.header"))));

        output.append(processItem("Драйвер базы данных", PrimPlugin.driver.isWorking(), placeholders));
        ModuleManager.cache.forEach((name, module) -> output.append(processItem(
                name, module.getModule().enabled, placeholders)));

        output.append(Utils.parse(String.join("\n", PrimPlugin.i18n.getStringList("status.footer"))));
        return Utils.parse(output.toString(), placeholders);
    }

    /**
     * Получить сформированную строку состояние указанного элемента
     *
     * @param name            Название элемента
     * @param working         Работает ли элемент
     * @param placeholderList Список заполнителей
     * @return Сформированная строка
     */
    public String processItem(String name, boolean working, List<IPlaceholder> placeholderList) {
        String status = working ? "working" : "disabled";

        List<IPlaceholder> placeholders = new ArrayList<>(placeholderList);
        placeholders.add(new Placeholder("item", name));
        placeholders.add(new Placeholder("color", PrimPlugin.i18n.getString("status.statuses." + status + ".color")));
        placeholders.add(new Placeholder("status", PrimPlugin.i18n.getString("status.statuses." + status + ".name")));
        return Utils.parse(PrimPlugin.i18n.getString("status.content"), placeholders);
    }
}
