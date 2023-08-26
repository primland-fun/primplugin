package ru.primland.plugin.commands.reputation;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.OutputConstants;
import ru.primland.plugin.database.data.PrimPlayer;
import ru.primland.plugin.database.data.subdata.Reputation;
import ru.primland.plugin.utils.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CommandInfo(
        name="reputation",
        description="Управление репутацией игроков",
        aliases={"rep"}
)
public class ReputationCommand extends Command {
    public static Config config;

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        config = Config.load("reputation.yml");
    }

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {
        config = null;
    }

    /**
     * Выполнить команду с указанными данными
     *
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    @Override
    public @Nullable String execute(CommandContext ctx) {
        return OutputConstants.help;
    }

    /**
     * Выполнить операцию с репутацией
     *
     * @param ctx       Контекст команды
     * @param errorType in/ax
     * @param def       Значение по умолчанию для максимальной/минимальной репутации
     * @param value     Значение, которое нужно прибавить к текущей репутации игрока
     *                  (чтобы отнять, укажите отрицательное)
     * @param message   give/take
     * @return Строка-ответ
     */
    public static @Nullable String executeOperation(@NotNull CommandContext ctx, String errorType, int def, int value, String message) {
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", ctx.sender.getName()));

        PrimPlayer player = Objects.requireNonNull(ctx.get("player"));
        placeholders.add(new Placeholder("player", player.getName()));

        Reputation reputation = player.getReputation();
        if(reputation.getValue() == ReputationCommand.config.getInteger("m" + errorType + "Reputation", def)) {
            return Utils.parse(ReputationCommand.config.getString("errors.reputationM%sLimit"
                    .formatted(errorType)), placeholders);
        }

        PrimPlayer primSender = PrimPlugin.driver.getPlayer(ctx.sender.getName());
        if(primSender == null)
            return PrimPlugin.i18n.getString("youNotFound");

        if(!primSender.canUseReputationCommand())
            return ReputationCommand.config.getString("errors.canNotUse");

        reputation.setValue(reputation.getValue()+value);
        player.updateReputation(reputation);

        Reputation sReputation = primSender.getReputation();
        sReputation.setLastAction(LocalDateTime.now());
        primSender.updateReputation(sReputation);
        return Utils.parse(ReputationCommand.config.getString("messages." + message), placeholders);
    }
}
