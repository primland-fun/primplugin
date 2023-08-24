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
        name="add",
        description="Добавить игрока в белый список",
        permission="primplugin.commands.whitelist.add",
        parent="plwl"
)
public class WhitelistAdd extends Command {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public void load(PrimPlugin plugin) {
        addArgument(new StringArgument("name", "ник игрока", true));
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
        // Получаем ник игрока, которого надо добавить в белый список
        String name = Objects.requireNonNull(ctx.get("name"));

        // Проверяем, есть ли игрок в белом списке, если да, то выдаём ошибку
        List<String> whitelist = WhitelistCommand.config.getStringList("whitelist");
        if(whitelist.contains(name)) {
            return Utils.parse(WhitelistCommand.config.getString("errors.playerExistsInWhitelist"),
                    new Placeholder("player", name));
        }

        whitelist.add(name);
        WhitelistCommand.config.set("whitelist", whitelist);
        WhitelistCommand.config.save();
        return Utils.parse(WhitelistCommand.config.getString("messages.add"),
                new Placeholder("player", name));
    }
}
