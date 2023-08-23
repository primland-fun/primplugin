package ru.primland.plugin.commands;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.argument.Arguments;
import ru.primland.plugin.commands.manager.ICommand;
import ru.primland.plugin.commands.manager.argument.Argument;
import ru.primland.plugin.commands.manager.argument.ArgumentSuggestion;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.database.data.PrimPlayer;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CommandInfo(
        name="msg",
        description="Отправить приватное сообщение любому другому игроку",
        aliases={"w", "pm", "dm"},
        arguments={
                // При указании Player как тип getValue() будет возвращать строку
                @Argument(name="receiver", type=Player.class, displayName="игрок", required=true),
                @Argument(name="message", type=String.class, stringIsText=true, displayName="сообщение",
                        required=true, dynamicSuggestion=ArgumentSuggestion.ONLINE_PLAYERS)
        }
)
public class PrivateMessage implements ICommand {
    // TODO: слежка (логи приватных сообщений)

    /**
     * Объект конфигурации приватных сообщений
     */
    public static Config config;

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public void load(PrimPlugin plugin) {
        config = Config.load("commands/private_messages.yml");
    }

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public void unload(PrimPlugin plugin) {
        config = null;
    }

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

        // Получаем ник получателя (нам не нужно проверять всё это, т.к. это делается
        // автоматически)
        String player = (String) Objects.requireNonNull(args.getArgument("receiver")).getValue();

        placeholders.add(new Placeholder("player", player));
        placeholders.add(new Placeholder("receiver", player));

        // Проверяем, не указал ли игрок самого себя (с шизой может и в реальность
        // пообщаться)
        if(player.equals(sender.getName()))
            return Utils.parse(PrimPlugin.i18n.getString("selfSpecified"), placeholders);

        Player receiver = Bukkit.getPlayer(player);
        PrimPlayer primPlayer = PrimPlugin.driver.getPlayer(player);

        // Проверяем, можем ли мы отправить указанному игроку приватное сообщение
        if(receiver == null && primPlayer == null)
            return Utils.parse(PrimPlugin.i18n.getString("playerNotFound"), placeholders);

        // Получаем сообщение и добавляем его как заполнитель
        String message = String.valueOf(Objects.requireNonNull(args.getArgument("message")).getValue());
        placeholders.add(new Placeholder("message", message));

        sender.sendMessage(Utils.parse(config.getString("sender.message"), placeholders));
        if(receiver != null) {
            receiver.sendMessage(Utils.parse(config.getString("receiver.message"), placeholders));
            Utils.playSound(receiver, primPlayer.getChat().getSound());
            return null;
        }

        primPlayer.sendMessage(sender.getName(), message);
        return null;
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
