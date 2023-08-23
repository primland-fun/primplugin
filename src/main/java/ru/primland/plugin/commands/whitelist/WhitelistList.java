package ru.primland.plugin.commands.whitelist;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.argument.Arguments;
import ru.primland.plugin.commands.manager.ICommand;
import ru.primland.plugin.commands.manager.argument.Argument;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.utils.Utils;

import java.util.List;

@CommandInfo(
        name="list",
        description="Получить список игроков в белом списке",
        permission="primplugin.commands.whitelist.list",
        parent="plwl"
)
public class WhitelistList implements ICommand {
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
    public @Nullable String execute(CommandSender sender, Arguments args) {
        String players = String.join(WhitelistCommand.config.getString("messages.listSeparator", ", "),
                WhitelistCommand.config.getStringList("whitelist"));

        return Utils.parse(WhitelistCommand.config.getString("messages.list"),
                new Placeholder("count", WhitelistCommand.config.getStringList("whitelist").size()),
                new Placeholder("players", players));
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
