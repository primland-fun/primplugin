package ru.primland.plugin.commands;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.argument.type.PlayerArgument;
import ru.primland.plugin.commands.manager.argument.type.TextArgument;
import ru.primland.plugin.database.data.PrimPlayer;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CommandInfo(
        name="msg",
        description="Отправить приватное сообщение любому другому игроку",
        aliases={"w", "pm", "dm"}
)
public class PrivateMessage extends Command {
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
        addArgument(new PlayerArgument<PrimPlayer>("receiver", "игрок", true));
        addArgument(new TextArgument("message", "сообщение", true));
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
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    public @Nullable String execute(@NotNull CommandContext ctx) {
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", ctx.sender.getName()));

        // Получаем ник получателя (нам не нужно проверять всё это, т.к. это делается
        // автоматически)
        PrimPlayer player = Objects.requireNonNull(ctx.get("receiver"));
        placeholders.add(new Placeholder("player", player));
        placeholders.add(new Placeholder("receiver", player));

        // Проверяем, не указал ли игрок самого себя (с шизой может и в реальность
        // пообщаться)
        if(player.getName().equals(ctx.sender.getName()))
            return Utils.parse(PrimPlugin.i18n.getString("selfSpecified"), placeholders);

        Player receiver = Bukkit.getPlayer(player.getName());

        // Получаем сообщение и добавляем его как заполнитель
        String message = Objects.requireNonNull(ctx.get("message"));
        placeholders.add(new Placeholder("message", message));

        ctx.send(config.getString("sender.message"), placeholders);
        if(receiver != null) {
            receiver.sendMessage(Utils.parse(config.getString("receiver.message"), placeholders));
            Utils.playSound(receiver, player.getChat().getSound());
            return null;
        }

        player.sendMessage(ctx.sender.getName(), message);
        return null;
    }
}
