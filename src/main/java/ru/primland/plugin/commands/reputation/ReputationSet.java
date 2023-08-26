package ru.primland.plugin.commands.reputation;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.argument.type.IntegerArgument;
import ru.primland.plugin.commands.manager.argument.type.PlayerArgument;
import ru.primland.plugin.database.data.PrimPlayer;
import ru.primland.plugin.database.data.subdata.Reputation;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CommandInfo(
        name="set",
        description="Установить репутацию игрока",
        permission="primplugin.commands.rep.set",
        parent="reputation"
)
public class ReputationSet extends Command {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        addArgument(new PlayerArgument<PrimPlayer>("player", "игрок", true, false));
        addArgument(new IntegerArgument("count", "количество", true));
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

        PrimPlayer player = Objects.requireNonNull(ctx.get("player"));
        placeholders.add(new Placeholder("player", player.getName()));

        int count = Objects.requireNonNull(ctx.get("count"));
        placeholders.add(new Placeholder("count", count));

        if(count == ReputationCommand.config.getInteger("maxReputation", 100))
            return Utils.parse(ReputationCommand.config.getString("errors.reputationMaxLimit"), placeholders);

        if(count == ReputationCommand.config.getInteger("minReputation", -100))
            return Utils.parse(ReputationCommand.config.getString("errors.reputationMinLimit"), placeholders);

        Reputation reputation = player.getReputation();
        reputation.setValue(count);
        player.updateReputation(reputation);
        return Utils.parse(ReputationCommand.config.getString("messages.set"), placeholders);
    }
}
