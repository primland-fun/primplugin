package ru.primland.plugin.commands.plugin;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.modules.IPluginModule;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements IPluginCommand {
    /**
     * Выполняет команду плагина
     * @param sender Отправитель команды
     * @param args Аргументы команды
     */
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull List<String> args) {
        PrimPlugin plugin = PrimPlugin.getInstance();
        Config config = Config.load("commands/plugin_help.yml");
        StringBuilder message = new StringBuilder();

        if(args.size() == 0) {
            message.append(String.join("\n", config.getStringList("headers.modules"))).append("\n");
            message.append(Utils.parse(config.getString("content.module"), new Placeholder("name", "primary"),
                    new Placeholder("description", "Основные команды плагина"),
                    new Placeholder("commands", plugin.getPrimaryCommand().getCommands().size()- plugin.getManager()
                            .getModuleSubCommands().size())));

            plugin.getManager().getModules().forEach(module -> {
                if(config.getBoolean("ignoreModulesWithoutCommands", true) && plugin.getManager().getModuleSubCommands(module.getName()).size() == 0)
                    return;

                message.append("\n");
                message.append(Utils.parse(config.getString("content.module"), new Placeholder("name", module.getName()),
                        new Placeholder("description", module.getDescription()),
                        new Placeholder("commands", plugin.getManager().getModuleSubCommands(module
                                .getName()).size())));
            });

            message.append(String.join("\n", config.getStringList("footer")));
            sender.sendMessage(Utils.parse(message.toString()));
            return;
        }

        String name = args.get(0);

        List<String> header = new ArrayList<>();
        config.getStringList("headers.commands").forEach(line -> header.add(Utils.parse(line,
                new Placeholder("module", name))));

        message.append(String.join("\n", header)).append("\n");
        if(name.equals("primary")) {
            message.append(commandsToString(sender, plugin.getPrimaryCommand().getCommands()));
            message.append(String.join("\n", config.getStringList("footer")));
            sender.sendMessage(Utils.parse(message.toString()));
            return;
        }

        IPluginModule module = plugin.getManager().getModule(name);
        if(module == null) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.moduleNotFound"),
                    new Placeholder("module", name)));

            return;
        }

        if(plugin.getManager().getModuleSubCommands(module.getName()).size() == 0) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.moduleHasNotCommands"),
                    new Placeholder("module", name)));

            return;
        }

        message.append(commandsToString(sender, plugin.getManager().getModuleSubCommands(module.getName())));
        message.append(String.join("\n", config.getStringList("footer")));
        sender.sendMessage(Utils.parse(message.toString()));
    }

    /**
     * Конвертирует список команд в строку
     * @param sender Отправитель команды
     * @param commands Список команд
     * @return Строка с командами
     */
    private @NotNull String commandsToString(CommandSender sender, @NotNull List<IPluginCommand> commands) {
        Config config = Config.load("commands/plugin_help.yml");

        StringBuilder output = new StringBuilder();
        commands.forEach(cmd -> {
            if(PluginPrimaryCommand.hasNotPermissions(sender, cmd.getRequiredPermissions()))
                return;

            output.append(Utils.parse(config.getString("content.command"),
                    new Placeholder("name", cmd.getName()),
                    new Placeholder("usage", cmd.getUsage().length() > 0 ? " " + cmd.getUsage() : ""),
                    new Placeholder("description", cmd.getDescription())));
        });

        return output.toString();
    }

    /**
     * "Подсказывает" игроку значение аргумента
     * @param sender Отправитель команды
     * @param args Аргументы команды
     * @return Список с подсказками для аргумента
     */
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if(Math.max(args.length-1, 0) == 1) {
            List<String> modules = PrimPlugin.getInstance().getManager().getModulesNames();
            modules.add(0, "primary");
            return modules;
        }

        return null;
    }

    /**
     * Получает и возвращает название этой команды
     * @return Название команды
     */
    @Override
    public String getName() {
        return "help";
    }

    /**
     * Получает и возвращает описание этой команды
     * @return Описание команды
     */
    @Override
    public String getDescription() {
        return "Получение помощи по командам";
    }

    /**
     * Получает и возвращает разрешения, требуемые для выполнения
     * этой команды
     * @return Требуемые разрешения
     */
    @Override
    public List<String> getRequiredPermissions() {
        return List.of("primplugin.commands.primary");
    }

    /**
     * Получает и возвращает инструкцию по использованию этой команды
     * @return Инструкция по использованию команды
     */
    @Override
    public String getUsage() {
        return "[модуль]";
    }
}
