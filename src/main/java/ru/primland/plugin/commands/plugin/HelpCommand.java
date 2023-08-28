package ru.primland.plugin.commands.plugin;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.*;
import ru.primland.plugin.commands.manager.argument.type.IntegerArgument;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@CommandInfo(
        name="help",
        description="Помощь по командам плагина",
        parent="primplugin"
)
public class HelpCommand extends Command {
    private static int commandsAtPage;

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        addArgument(new IntegerArgument("page", "номер страницы", false));
        commandsAtPage = PrimPlugin.i18n.getInteger("commandsAtPage", 6);
    }

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {
        commandsAtPage = 0;
    }

    /**
     * Выполнить команду с указанными данными
     *
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    @Override
    public @Nullable String execute(@NotNull CommandContext ctx) {
        @Nullable Integer page = ctx.get("page");
        return getResult(ctx, page == null ? 1 : page);
    }

    /**
     * Получить результат для команды /pp help
     *
     * @param ctx  Контекст команды
     * @param page Номер страницы с командами
     * @return Готовый результат
     */
    public static @NotNull String getResult(CommandContext ctx, int page) {
        // TODO: Доделать EpsilonEngine и закинуть сюда кликабельные компоненты
        Map<String, CachedCommand> commands = CommandManager.cache;

        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("page", page));
        placeholders.add(new Placeholder("pages", commands.size() / commandsAtPage));

        StringBuilder output = new StringBuilder(Utils.parse(String.join("\n",
                PrimPlugin.i18n.getStringList("globalHelp.header"))));

        AtomicInteger index = new AtomicInteger(0);
        commands.forEach((name, command) -> {
            if(index.get() >= commandsAtPage)
                return;

            CommandInfo info = command.getInfo();
            if(!ctx.sender.hasPermission(info.permission()))
                return;

            if(info.playersOnly() && ctx.sender instanceof ConsoleCommandSender)
                return;

            List<IPlaceholder> cmdPlaceholders = new ArrayList<>(placeholders);
            cmdPlaceholders.add(new Placeholder("name", (info.parent().isEmpty() ? "" : info.parent() + " ") + name));
            cmdPlaceholders.add(new Placeholder("description", info.description()));
            cmdPlaceholders.add(new Placeholder("aliases", String.join(", ", info.aliases())));
            cmdPlaceholders.add(new Placeholder("permission", info.permission()));
            cmdPlaceholders.add(new Placeholder("usage", CommandManager.getUsage(command.getCommand())));

            output.append(Utils.parse(PrimPlugin.i18n.getString("globalHelp.content"), cmdPlaceholders));
            index.getAndIncrement();
        });

        output.append(Utils.parse(String.join("\n", PrimPlugin.i18n.getStringList("globalHelp.footer"))));
        return Utils.parse(output.toString(), placeholders);
    }
}
