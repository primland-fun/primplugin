package ru.primland.plugin.commands.reputation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.argument.type.PlayerArgument;
import ru.primland.plugin.database.data.PrimPlayer;

@CommandInfo(
        name="+",
        description="Press F to p... Дать другому игроку 1 репутацию",
        parent="reputation"
)
public class ReputationPlus extends Command {
    // TODO: calc remaining time and use in error

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public void load(PrimPlugin plugin) {
        addArgument(new PlayerArgument<PrimPlayer>("player", "игрок", true, true));
    }

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public void unload(PrimPlugin plugin) {}

    /**
     * Выполнить команду с указанными данными
     *
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    public @Nullable String execute(@NotNull CommandContext ctx) {
        return ReputationCommand.executeOperation(ctx, "ax", 100, 1, "give");
    }
}
