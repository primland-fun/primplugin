package ru.primland.plugin.commands.whitelist;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Arguments;
import ru.primland.plugin.commands.manager.ICommand;
import ru.primland.plugin.commands.manager.annotations.Argument;
import ru.primland.plugin.commands.manager.annotations.CommandInfo;
import ru.primland.plugin.utils.Utils;

import java.util.List;
import java.util.Objects;

@CommandInfo(
        name="add",
        description="Добавить игрока в белый список",
        permission="primplugin.commands.whitelist.add",
        parent="plwl",
        arguments={@Argument(name="name", type=String.class, displayName="ник игрока", required=true)}
)
public class WhitelistAdd implements ICommand {
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
    public @Nullable String execute(CommandSender sender, @NotNull Arguments args) {
        // Получаем ник игрока, которого надо добавить в белый список
        String name = (String) Objects.requireNonNull(args.getArgument("name")).getValue();

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
