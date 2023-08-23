package ru.primland.plugin.commands.whitelist;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.argument.Arguments;
import ru.primland.plugin.commands.manager.ICommand;
import ru.primland.plugin.commands.manager.OutputConstants;
import ru.primland.plugin.commands.manager.argument.Argument;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.utils.Utils;

import java.util.List;

@CommandInfo(
        name="plwl",
        description="Команда для управления белым списком",
        aliases={"wl"},
        permission="primplugin.commands.whitelist"
)
public class WhitelistCommand implements ICommand {
    // TODO: Логи

    /**
     * Объект конфигурации белого списка
     */
    public static Config config;

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        config = Config.load("commands/whitelist.yml");
    }

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {
        config = null;
    }

    /**
     * Выполнить команду с указанными данными
     *
     * @param sender    Отправитель команды
     * @param arguments Аргументы команды
     * @return Сообщение для отправителя команды
     */
    @Override
    public @Nullable String execute(CommandSender sender, Arguments arguments) {
        return OutputConstants.help;
    }

    /**
     * Получить подсказку для указанного аргумента
     *
     * @param previous Предыдущие аргументы
     * @param argument Данные об аргументе, подсказку для которого нужно получить
     * @return Список строк
     */
    @Override
    public List<String> getSuggestionsFor(Arguments previous, Argument argument) {
        return null;
    }

    /**
     * Содержит ли белый список указанного игрока
     *
     * @param player Объект игрока
     * @return true, если содержит, иначе false
     */
    public static boolean whitelistContains(@NotNull Player player) {
        return config.getStringList("whitelist").contains(player.getName());
    }

    /**
     * Получить причину для кика игрока, которого нет в белом списке
     * @return Причина
     */
    public String getReason() {
        return Utils.parse(String.join("\n", config.getStringList("messages.kick")));
    }
}
