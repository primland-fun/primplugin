package ru.primland.plugin.commands.plugin;


import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;

@CommandInfo(
        name="primplugin",
        description="Основная команда плагина PrimPlugin",
        permission="primplugin.command.primary",
        aliases={"prim", "pp"}
)
public class PluginPrimaryCommand extends Command {
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
        return HelpCommand.getResult(ctx, 1);
    }
}
