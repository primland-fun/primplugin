package ru.primland.plugin.commands.reputation;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Arguments;
import ru.primland.plugin.commands.manager.ICommand;
import ru.primland.plugin.commands.manager.annotations.Argument;
import ru.primland.plugin.commands.manager.annotations.CommandInfo;
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
        parent="rep",
        arguments={@Argument(name="player", type=Player.class, displayName="игрок", required=true)}
)
public class ReputationPlus implements ICommand {
    // TODO: calc remaining time and use in error

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
     * @param sender Отправитель команды
     * @param args   Аргументы команды
     * @return Сообщение для отправителя команды
     */
    @Override
    public @Nullable String execute(@NotNull CommandSender sender, @NotNull Arguments args) {
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", sender.getName()));

        String player = (String) Objects.requireNonNull(args.getArgument("player")).getValue();
        placeholders.add(new Placeholder("player", player));
        if(player.equals(sender.getName()))
            return Utils.parse(PrimPlugin.i18n.getString("selfSpecified"), placeholders);

        PrimPlayer primPlayer = PrimPlugin.driver.getPlayer(player);
        if(primPlayer == null)
            return Utils.parse(PrimPlugin.i18n.getString("playerNotFound"), placeholders);

        Reputation reputation = primPlayer.getReputation();
        if(reputation.getValue() == ReputationCommand.config.getInteger("maxReputation", 100)) {
            return Utils.parse(ReputationCommand.config.getString("errors.reputationMaxLimit"),
                    placeholders);
        }

        PrimPlayer primSender = PrimPlugin.driver.getPlayer(sender.getName());
        if(primSender == null)
            return PrimPlugin.i18n.getString("youNotFound");

        if(!primSender.canUseReputationCommand())
            return ReputationCommand.config.getString("errors.canNotUse");

        reputation.setValue(reputation.getValue()+1);
        primPlayer.updateReputation(reputation);

        Reputation sReputation = primSender.getReputation();
        sReputation.setLastAction(LocalDateTime.now());
        primSender.updateReputation(sReputation);
        return Utils.parse(ReputationCommand.config.getString("messages.give"), placeholders);
    }

    /**
     * Получить подсказку для указанного аргумента
     *
     * @param previous Предыдущие аргументы
     * @param argument Данные об аргументе, подсказку для которого нужно получить
     * @return Список строк
     */
    @Override
    public List<String> getSuggestionsFor(Arguments previous, Argument argument) {
        return null;
    }
}
