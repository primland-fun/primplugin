package ru.primland.plugin.commands.manager;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.argument.annotation.Argument;
import ru.primland.plugin.utils.Utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Менеджер команд
 */
public class CommandManager {
    private static Map<String, CachedCommand> cache;
    private static CommandMap commandMap;

    /**
     * Инициализировать менеджер.
     * Данная функция создаёт кеш и получает доступ к карте команд
     */
    public static void init() {
        cache = new HashMap<>();
        commandMap = getCommandMap();

        if(commandMap == null) {
            PrimPlugin.send(PrimPlugin.i18n.getString("commandMapFailed"));
            PrimPlugin.instance.getServer().getPluginManager().disablePlugin(PrimPlugin.instance);
        }
    }

    /**
     * Зарегистрировать все команды из этого проекта (просто ищет все реализации
     * ICommand и вызывает функции регистрации для каждой)
     */
    public static void registerAll() {
        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);
        loader.forEach(CommandManager::register);
        checkSubCommands();
    }

    /**
     * Проверяет все подкоманды на то, чтобы у них всех был указан существующий
     * родительский элемент.<br>
     * <br>
     * Если же это не так, но пишет об этом в консоль и снимает регистрацию с команды
     */
    public static void checkSubCommands() {
        cache.forEach((key, value) -> {
            if(value.getInfo().parent().isEmpty())
                return;

            if(cache.containsKey(value.getInfo().parent()))
                return;

            unregister(key);
        });
    }

    /**
     * Зарегистрировать указанную команду
     *
     * @param command Объект реализации команды
     */
    public static void register(@NotNull Command command) {
        // Получаем и проверяем информацию о команде
        CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
        if(info == null) {
            PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("commandInfoNotFound"),
                    new Placeholder("class", command.getClass().getName())));

            PrimPlugin.instance.getServer().getPluginManager().disablePlugin(PrimPlugin.instance);
            return;
        }

        // Создаём экземпляр BukkitCommand, регистрируем его как команду и добавляем
        // в кеш
        BukkitCommand bukkitCommand = new BukkitCommand(command, info);
        commandMap.register(info.name(), bukkitCommand);
        cache.put(info.name(), new CachedCommand(command, info, bukkitCommand));
    }

    /**
     * Снять регистрацию со всех команд
     */
    public static void unregisterAll() {
        cache.keySet().forEach(CommandManager::unregister);
    }

    /**
     * Снять регистрацию с указанной команды
     *
     * @param commandName Название команды
     */
    public static void unregister(@NotNull String commandName) {
        CachedCommand command = cache.get(commandName);
        if(command == null)
            return;

        command.getBukkitCommand().unregister(commandMap);
        command.getCommand().unload(PrimPlugin.instance);
        cache.remove(commandName);
    }

    /**
     * Найти все подкоманды указанной родительской команды
     *
     * @param parent Название родительской команды
     * @return Карта, где ключ - это название команды, а значение - это кеш-объект
     *         команды
     */
    public static @NotNull Map<String, CachedCommand> searchSubCommands(@NotNull String parent) {
        return Utils.search(cache, (command) -> command.getInfo().parent().equals(parent));
    }

    /**
     * Получить доступ к карте команд
     * @return {@link CommandMap}
     */
    private static @Nullable CommandMap getCommandMap() {
        if(!(Bukkit.getPluginManager() instanceof SimplePluginManager))
            return null;

        try {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getPluginManager());
        } catch(NoSuchFieldException | IllegalAccessException error) {
            return null;
        }
    }

    /**
     * Сформировать строку использования для команды
     *
     * @param info Информация о команде
     * @return Сформированная строка использования
     */
    public static @NotNull String getUsage(@NotNull CommandInfo info) {
        StringBuilder builder = new StringBuilder();
        for(Argument argument : info.arguments()) {
            builder.append(argument.required() ? "{" : "[");
            builder.append(argument.displayName().isEmpty() ? argument.name() : argument.displayName());
            builder.append(argument.required() ? "}" : "]");
            builder.append(" ");
        }

        return (builder.charAt(builder.length()-1) == ' ' ? builder.deleteCharAt(
                builder.length()-1) : builder).toString();
    }
}
