package ru.primland.plugin.commands.plugin;

import org.bukkit.command.CommandSender;

import org.jetbrains.annotations.NotNull;

import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.Utils;
import ru.primland.plugin.modules.IPluginModule;
import ru.primland.plugin.modules.ModuleManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InfoCommand implements IPluginCommand {
    /**
     * Выполняет команду плагина
     * @param sender Отправитель команды
     * @param args Аргументы команды
     */
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull List<String> args) {
        if(args.size() == 0) {
            PrimPlugin.send(sender, "&cИзвините, но вы забыли указать модуль");
            return;
        }

        ModuleManager manager = PrimPlugin.getInstance().getManager();
        IPluginModule module = manager.getModule(args.get(0));
        if(module == null) {
            PrimPlugin.send(sender, "&cИзвините, но вы указали несуществующий модуль");
            return;
        }

        String info = module.information();
        if(info == null || info.length() == 0) {
            PrimPlugin.send(sender, "&cИзвините, но данный модуль не имеет информации");
            return;
        }

        sender.sendMessage(Utils.translate(info));
    }

    /**
     * "Подсказывает" игроку значение аргумента
     * @param sender Отправитель команды
     * @param args Аргументы команды
     * @return Список с подсказками для аргумента
     */
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        Collection<IPluginModule> modules = PrimPlugin.getInstance().getManager().getModules();
        List<String> names = new ArrayList<>();
        modules.forEach(module -> {
            String info = module.information();
            if(info == null || info.length() == 0) return;
            names.add(module.getName());
        });

        return names;
    }

    /**
     * Получает и возвращает название этой команды
     * @return Название команды
     */
    @Override
    public String getName() {
        return "info";
    }

    /**
     * Получает и возвращает описание этой команды
     * @return Описание команды
     */
    @Override
    public String getDescription() {
        return "Получить информацию о модуле плагина";
    }

    /**
     * Получает и возвращает разрешения, требуемые для выполнения
     * этой команды
     * @return Требуемые разрешения
     */
    @Override
    public List<String> getRequiredPermissions() {
        return List.of("primplugin.commands.info");
    }

    /**
     * Получает и возвращает инструкцию по использованию этой команды
     * @return Инструкция по использованию команды
     */
    @Override
    public String getUsage() {
        return "{модуль}";
    }
}
