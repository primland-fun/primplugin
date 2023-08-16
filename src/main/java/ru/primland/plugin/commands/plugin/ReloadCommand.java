package ru.primland.plugin.commands.plugin;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.PrimPlugin;

import java.util.List;

public class ReloadCommand implements IPluginCommand {
    /**
     * Выполняет команду плагина
     * @param sender Отправитель команды
     * @param args Аргументы команды
     */
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull List<String> args) {
        PrimPlugin.getInstance().reload();
        PrimPlugin.send(sender, PrimPlugin.getInstance().getI18n().getString("reload"));
    }

    /**
     * "Подсказывает" игроку значение аргумента
     * @param sender Отправитель команды
     * @param args Аргументы команды
     * @return Список с подсказками для аргумента
     */
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }

    /**
     * Получает и возвращает название этой команды
     * @return Название команды
     */
    @Override
    public String getName() {
        return "reload";
    }

    /**
     * Получает и возвращает описание этой команды
     * @return Описание команды
     */
    @Override
    public String getDescription() {
        return "Перезагрузить весь плагин";
    }

    /**
     * Получает и возвращает разрешения, требуемые для выполнения
     * этой команды
     * @return Требуемые разрешения
     */
    @Override
    public List<String> getRequiredPermissions() {
        return List.of("primplugin.commands.reload");
    }

    /**
     * Получает и возвращает инструкцию по использованию этой команды
     * @return Инструкция по использованию команды
     */
    @Override
    public String getUsage() {
        return "";
    }
}
