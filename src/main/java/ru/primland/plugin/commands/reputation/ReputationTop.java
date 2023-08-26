package ru.primland.plugin.commands.reputation;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.utils.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@CommandInfo(
        name="top",
        description="Топ игроков по репутации",
        parent="reputation"
)
public class ReputationTop extends Command {
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
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    @Override
    public @Nullable String execute(@NotNull CommandContext ctx) {
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", ctx.sender.getName()));

        Config config = ReputationCommand.config;
        StringBuilder content = new StringBuilder();
        content.append(Utils.parse(String.join("\n", config.getStringList("messages.top.header"))));

        boolean foundSender = false;
        ResultSet top = PrimPlugin.driver.getTopByReputation(10);
        try {
            int number = 1;
            while(top.next()) {
                content.append("\n");

                String name = top.getString("name");
                int reputation = top.getInt("reputation.value");

                String key = "messages.top.content";
                if(name.equals(ctx.sender.getName())) {
                    foundSender = true;
                    key += "Player";
                }

                content.append(Utils.parse(config.getString(key), new Placeholder("number", number),
                        new Placeholder("player", name), new Placeholder("reputation", reputation)));

                number++;
            }
        } catch(SQLException error) {
            error.printStackTrace();
        }

        if(!foundSender) {
            int value = PrimPlugin.driver.getPlayer(ctx.sender.getName()).getReputation().getValue();
            content.append("\n");
            content.append(Utils.parse(config.getString("messages.top.contentPlayer"),
                    new Placeholder("number", "~~"),
                    new Placeholder("player", ctx.sender.getName()),
                    new Placeholder("reputation", value)));
        }

        content.append(Utils.parse(String.join("\n", config.getStringList("messages.top.footer"))));
        return Utils.parse(content.toString(), placeholders);
    }
}
