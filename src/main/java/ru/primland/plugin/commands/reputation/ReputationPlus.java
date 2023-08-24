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
import ru.primland.plugin.database.data.subdata.Reputation;
import ru.primland.plugin.utils.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CommandInfo(
        name="+",
        description="Press F to p... Дать другому игроку 1 репутацию",
        parent="rep"
)
public class ReputationPlus extends Command {
    // TODO: calc remaining time and use in error

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public void load(PrimPlugin plugin) {
        addArgument(new PlayerArgument<PrimPlayer>("player", "игрок", true));
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
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", ctx.sender.getName()));

        PrimPlayer player = Objects.requireNonNull(ctx.get("player"));
        placeholders.add(new Placeholder("player", player.getName()));
        if(player.getName().equals(ctx.sender.getName()))
            return Utils.parse(PrimPlugin.i18n.getString("selfSpecified"), placeholders);

        Reputation reputation = player.getReputation();
        if(reputation.getValue() == ReputationCommand.config.getInteger("maxReputation", 100)) {
            return Utils.parse(ReputationCommand.config.getString("errors.reputationMaxLimit"),
                    placeholders);
        }

        PrimPlayer primSender = PrimPlugin.driver.getPlayer(ctx.sender.getName());
        if(primSender == null)
            return PrimPlugin.i18n.getString("youNotFound");

        if(!primSender.canUseReputationCommand())
            return ReputationCommand.config.getString("errors.canNotUse");

        reputation.setValue(reputation.getValue()+1);
        player.updateReputation(reputation);

        Reputation sReputation = primSender.getReputation();
        sReputation.setLastAction(LocalDateTime.now());
        primSender.updateReputation(sReputation);
        return Utils.parse(ReputationCommand.config.getString("messages.give"), placeholders);
    }
}
