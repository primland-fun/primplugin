package ru.primland.plugin.commands.whitelist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.OutputConstants;
import ru.primland.plugin.utils.Utils;

@CommandInfo(
        name="plwl",
        description="Команда для управления белым списком",
        aliases={"wl"},
        permission="primplugin.commands.whitelist"
)
public class WhitelistCommand extends Command {
    // TODO: Логи

    /**
     * Объект конфигурации белого списка
     */
    public static Config config;

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public void load(PrimPlugin plugin) {
        config = Config.load("commands/whitelist.yml");
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
        return OutputConstants.help;
    }

    /**
     * Содержит ли белый список указанного игрока
     *
     * @param player Ник игрока
     * @return true, если содержит, иначе false
     */
    public static boolean whitelistContains(@NotNull String player) {
        return config.getStringList("whitelist").contains(player);
    }

    /**
     * Получить причину для кика игрока, которого нет в белом списке
     * @return Причина
     */
    public static @NotNull String getReason() {
        return Utils.parse(String.join("\n", config.getStringList("messages.kick")));
    }
}
