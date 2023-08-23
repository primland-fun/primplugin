package ru.primland.plugin.commands.manager;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.argument.Argument;
import ru.primland.plugin.commands.manager.argument.Arguments;

import java.util.List;

/**
 * Интерфейс для команд
 */
public interface ICommand {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    void load(PrimPlugin plugin);

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    void unload(PrimPlugin plugin);

    /**
     * Выполнить команду с указанными данными
     *
     * @param sender Отправитель команды
     * @param args   Аргументы команды
     * @return Сообщение для отправителя команды
     */
    @Nullable String execute(CommandSender sender, Arguments args);

    /**
     * Получить подсказку для указанного аргумента
     *
     * @param previous Предыдущие аргументы
     * @param argument Данные об аргументе, подсказку для которого нужно получить
     * @return Список строк
     */
    List<String> getSuggestionsFor(Arguments previous, Argument argument);
}
