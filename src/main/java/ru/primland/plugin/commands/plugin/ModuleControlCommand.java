package ru.primland.plugin.commands.plugin;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.modules.IPluginModule;
import ru.primland.plugin.modules.ModuleManager;
import ru.primland.plugin.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleControlCommand implements IPluginCommand {
    private static final Map<String, String> actions = new HashMap<>();

    public ModuleControlCommand() {
        actions.put("enable", "включён");
        actions.put("disable", "выключен");
        actions.put("reload", "перезагружен");
    }

    /**
     * Выполняет команду плагина
     * @param sender Отправитель команды
     * @param args  Аргументы команды
     */
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull List<String> args) {
        if(args.size() == 0) {
            PrimPlugin.send(sender, "&cУкажите действие, которое нужно выполнить с модулем!");
            return;
        }

        String action = args.get(0);
        if(!Utils.equalsOne(action, "enable", "disable", "reload")) {
            PrimPlugin.send(sender, "&cВы указали некорректное действие!");
            return;
        }

        if(args.size() == 1) {
            PrimPlugin.send(sender, "&cУкажите модуль, с которым нужно выполнить указанное действие!");
            return;
        }

        ModuleManager manager = PrimPlugin.getInstance().getManager();
        String module = args.get(1);
        if(!manager.getModulesNames().contains(module)) {
            PrimPlugin.send(sender, "&cУказанный модуль не существует!");
            return;
        }

        try {
            IPluginModule moduleObject = manager.getModule(module);
            if(Utils.equalsOne(action, "disable", "reload") && !moduleObject.isEnabled()) {
                PrimPlugin.send(sender, "&cМодуль &r", module, "&cвыключен ранее!");
                return;
            }

            if(action.equals("enable") && moduleObject.isEnabled()) {
                PrimPlugin.send(sender, "&cМодуль &r", module, "&cбыл включён ранее!");
                return;
            }

            if(Utils.equalsOne(action, "disable", "reload"))
                manager.disable(module, false);

            if(Utils.equalsOne(action, "enable", "reload"))
                manager.enable(module, false);

            PrimPlugin.send(sender, "Модуль &#65caef", module, " &rуспешно ", actions.get(action), "!");
        } catch(IllegalArgumentException ignored) {}
    }

    /**
     * "Подсказывает" игроку значение аргумента
     * @param sender Отправитель команды
     * @param args Аргументы команды
     * @return Список с подсказками для аргумента
     */
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        int argNumber = Math.max(args.length-1, 0);
        if(argNumber == 1)
            return List.of("enable", "disable", "reload");

        if(argNumber == 2 && args[1].equals("enable"))
            return PrimPlugin.getInstance().getManager().getModulesNames((m) -> !m.isEnabled());

        if(argNumber == 2 && Utils.equalsOne(args[1], "disable", "reload"))
            return PrimPlugin.getInstance().getManager().getModulesNames(IPluginModule::isEnabled);

        return List.of();
    }

    /**
     * Получает и возвращает название этой команды
     * @return Название команды
     */
    @Override
    public String getName() {
        return "modules";
    }

    /**
     * Получает и возвращает описание этой команды
     * @return Описание команды
     */
    @Override
    public String getDescription() {
        return "Команда для управления модулями плагина";
    }

    /**
     * Получает и возвращает разрешения, требуемые для выполнения этой команды
     * @return Требуемые разрешения
     */
    @Override
    public List<String> getRequiredPermissions() {
        return List.of("primplugin.commands.modules");
    }

    /**
     * Получает и возвращает инструкцию по использованию этой команды
     * @return Инструкция по использованию команды
     */
    @Override
    public String getUsage() {
        return "{команда} {модуль}";
    }
}
