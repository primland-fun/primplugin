package ru.primland.plugin.commands.private_messages;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
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
import ru.primland.plugin.database.data.subdata.ChatOptions;
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
        addArgument(new PlayerArgument<PrimPlayer>("receiver", "игрок", true, true));
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
        send(ctx, Objects.requireNonNull(ctx.get("receiver")), Objects.requireNonNull(ctx.get("message")));
        return null;
    }

    /**
     * Отправить указанному игроку приватное сообщение
     *
     * @param ctx     Контекст команды
     * @param player  Объект игрока PrimLand
     * @param message Приватное сообщение
     */
    public static void send(@NotNull CommandContext ctx, @NotNull PrimPlayer player, String message) {
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", ctx.sender.getName()));
        placeholders.add(new Placeholder("player", player.getName()));
        placeholders.add(new Placeholder("receiver", player.getName()));
        placeholders.add(new Placeholder("message", message));

        Player receiver = Bukkit.getPlayer(player.getName());

        ChatOptions options = player.getChat();
        options.setLastReceived(ctx.sender.getName());
        player.updateChatOptions(options);

        ctx.send(config.getString("sender.message"), placeholders);
        if(receiver != null) {
            receiver.sendMessage(Utils.parse(config.getString("receiver.message"), placeholders));
            Utils.playSound(receiver, player.getChat().getSound(), SoundCategory.PLAYERS);
            return;
        }

        player.sendMessage(ctx.sender.getName(), message);
    }
}
