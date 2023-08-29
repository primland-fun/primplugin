package ru.primland.plugin.commands.private_messages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.argument.type.TextArgument;
import ru.primland.plugin.database.data.PrimPlayer;
import ru.primland.plugin.utils.Utils;

import java.util.Objects;

@CommandInfo(
        name="reply",
        description="Ответить автору последнего полученного приватного сообщения",
        aliases={"r"},
        playersOnly=true
)
public class ReplyCommand extends Command {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        addArgument(new TextArgument("message", "сообщение", true));
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
        PrimPlayer sender = PrimPlugin.driver.getPlayer(ctx.sender.getName());
        String receiver = sender.getChat().getLastReceived();
        if(receiver == null)
            return Utils.parse(PrivateMessage.config.getString("errors.noOneToSend"));

        PrivateMessage.send(ctx, PrimPlugin.driver.getPlayer(receiver), Objects.requireNonNull(ctx.get("message")));
        return null;
    }
}
