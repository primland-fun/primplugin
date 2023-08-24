package ru.primland.plugin.commands.whitelist;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.utils.Utils;

@CommandInfo(
        name="list",
        description="Получить список игроков в белом списке",
        permission="primplugin.commands.whitelist.list",
        parent="plwl"
)
public class WhitelistList extends Command {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public void load(PrimPlugin plugin) {}

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
        String players = String.join(WhitelistCommand.config.getString("messages.listSeparator", ", "),
                WhitelistCommand.config.getStringList("whitelist"));

        return Utils.parse(WhitelistCommand.config.getString("messages.list"),
                new Placeholder("count", WhitelistCommand.config.getStringList("whitelist").size()),
                new Placeholder("players", players));
    }
}
