package ru.primland.plugin.commands.whitelist;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.argument.type.StringArgument;
import ru.primland.plugin.utils.Utils;

import java.util.List;
import java.util.Objects;

@CommandInfo(
        name="remove",
        description="Удалить игрока из белого списка",
        permission="primplugin.commands.whitelist.remove",
        parent="plwl"
)
public class WhitelistRemove extends Command {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public void load(PrimPlugin plugin) {
        addArgument(new StringArgument("name", "ник игрока", true,
                (ctx) -> PrimPlugin.getOnlinePlayersNames()));
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
        // Получаем ник игрока, которого надо убрать из белого списка
        String name = Objects.requireNonNull(ctx.get("name"));

        // Проверяем, нет ли игрока в белом списке, если да, то выдаём ошибку
        List<String> whitelist = WhitelistCommand.config.getStringList("whitelist");
        if(!whitelist.contains(name)) {
            return Utils.parse(WhitelistCommand.config.getString("errors.playerNotExistsInWhitelist"),
                    new Placeholder("player", name));
        }

        whitelist.remove(name);
        WhitelistCommand.config.set("whitelist", whitelist);
        WhitelistCommand.config.save();
        return Utils.parse(WhitelistCommand.config.getString("messages.remove"),
                new Placeholder("player", name));
    }
}
