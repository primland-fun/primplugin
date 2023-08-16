package ru.primland.plugin.commands.plugin;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IPluginCommand {
    /**
     * Выполняет команду плагина
     * @param sender Отправитель команды
     * @param args Аргументы команды
     */
    void execute(@NotNull CommandSender sender, List<String> args);

    /**
     * "Подсказывает" игроку значение аргумента
     * @param sender Отправитель команды
     * @param args Аргументы команды
     * @return Список с подсказками для аргумента
     */
    List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Получает и возвращает название этой команды
     * @return Название команды
     */
    String getName();

    /**
     * Получает и возвращает описание этой команды
     * @return Описание команды
     */
    String getDescription();

    /**
     * Получает и возвращает разрешения, требуемые для выполнения
     * этой команды
     * @return Требуемые разрешения
     */
    List<String> getRequiredPermissions();

    /**
     * Получает и возвращает инструкцию по использованию этой команды
     * @return Инструкция по использованию команды
     */
    String getUsage();
}
