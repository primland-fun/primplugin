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
        name="-",
        description="Понизить репутацию другого игрока",
        parent="reputation"
)
public class ReputationMinus extends Command {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        addArgument(new PlayerArgument<PrimPlayer>("player", "игрок", true, true));
    }

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
    public @Nullable String execute(@NotNull CommandContext ctx) {
        return ReputationCommand.executeOperation(ctx, "in", -100, -1, "take");
    }
}
