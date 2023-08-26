package ru.primland.plugin.commands.reputation;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.argument.type.PlayerArgument;
import ru.primland.plugin.database.data.PrimPlayer;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(
        name="get",
        description="Узнать свою/любого другого игрока репутацию",
        parent="reputation"
)
public class ReputationGet extends Command {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        addArgument(new PlayerArgument<PrimPlayer>("player", "игрок", false, false));
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
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", ctx.sender.getName()));

        PrimPlayer ctxPlayer = ctx.get("player");
        PrimPlayer player = ctxPlayer == null ? PrimPlugin.driver.getPlayer(ctx.sender.getName()) : ctxPlayer;
        placeholders.add(new Placeholder("player", player.getName()));

        placeholders.add(new Placeholder("reputation", player.getReputation().getValue()));
        return Utils.parse(ReputationCommand.config.getString("messages.get." + (player.getName()
                .equals(ctx.sender.getName()) ? "sender" : "other")), placeholders);
    }
}
