package ru.primland.plugin.commands;

import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.Utils;
import ru.primland.plugin.database.MySQLDriver;
import ru.primland.plugin.database.data.subdata.Reputation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReputationCommand implements TabExecutor {
    private Config config;

    public ReputationCommand(Config config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        Config i18n = PrimPlugin.getInstance().getI18n();
        List<IPlaceholder> placeholders = new ArrayList<>();
        placeholders.add(new Placeholder("sender", sender.getName()));

        if(args.length == 0) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.specifySubCommand"), placeholders));
            return true;
        }

        String action = args[0];
        if(!Utils.equalsOne(action, "+", "-", "set", "get", "top")) {
            placeholders.add(new Placeholder("subcommand", action));
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.invalidSubCommand"), placeholders));
            return true;
        }

        if(action.equals("set") && !sender.hasPermission("primplugin.commands.rep.set")) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("notEnoughRights"), placeholders));
            return true;
        }

        if(args.length == 1 && !Utils.equalsOne(action, "get", "top")) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("specifyPlayer"), placeholders));
            return true;
        }

        String player = args.length == 1 ? sender.getName() : args[1];
        placeholders.add(new Placeholder("player", player));
        MySQLDriver driver = PrimPlugin.getInstance().getDriver();
        if(!driver.playerExists(player)) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("playerNotFound"), placeholders));
            return true;
        }

        if(Utils.equalsOne(action, "+", "-") && player.equals(sender.getName())) {
            PrimPlugin.send(sender, Utils.parse(i18n.getString("selfSpecified"), placeholders));
            return true;
        }

        if(!Utils.equalsOne(action, "+", "-", "get", "top") && args.length == 2) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.specifyCount"), placeholders));
            return true;
        }

        int value = driver.getReputation(player);
        if(action.equals("get")) {
            placeholders.add(new Placeholder("reputation", value));

            String i18nKey = "messages.get." + (player.equals(sender.getName()) ? "sender" : "other");
            PrimPlugin.send(sender, Utils.parse(config.getString(i18nKey), placeholders));
            return true;
        }

        if(action.equals("top")) {
            StringBuilder content = new StringBuilder();
            content.append(Utils.parse(String.join("\n", config.getStringList("messages.top.header"))));

            boolean foundSender = false;
            ResultSet top = driver.getTopByReputation(10);
            try {
                int number = 1;
                while(top.next()) {
                    content.append("\n");

                    String name = top.getString("name");
                    Reputation reputation = Reputation.fromJSON(top.getString("reputation"));

                    String i18nKey = "messages.top.content";
                    if(name.equals(sender.getName())) {
                        foundSender = true;
                        i18nKey += "Player";
                    }

                    content.append(Utils.parse(config.getString(i18nKey), new Placeholder("number", number),
                            new Placeholder("player", name), new Placeholder("reputation", reputation.getValue())));

                    number++;
                }
            } catch(SQLException error) {
                error.printStackTrace();
            }

            if(!foundSender) {
                content.append("\n");
                content.append(Utils.parse(config.getString("messages.top.contentPlayer"),
                        new Placeholder("number", "~~"),
                        new Placeholder("player", sender.getName()),
                        new Placeholder("reputation", value)));
            }

            content.append(Utils.parse(String.join("\n", config.getStringList("messages.top.footer"))));
            sender.sendMessage(Utils.parse(content.toString(), placeholders));
            return true;
        }

        int count = action.equals("+") ? 1 : action.equals("-") ? -1 : Integer.parseInt(args[2]);

        if((action.equals("+") && value == config.getInteger("maxReputation", 100))
                || (action.equals("set") && count > config.getInteger("maxReputation", 100))) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.reputationMaxLimit"), placeholders));
            return true;
        }

        if((action.equals("-") && value == config.getInteger("minReputation", -100))
                || (action.equals("set") && count < config.getInteger("minReputation", -100))) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.reputationMinLimit"), placeholders));
            return true;
        }

        if(!driver.playerExists(sender.getName())) {
            PrimPlugin.send(sender, i18n.getString("youNotFound"));
            return true;
        }

        if(action.equals("+") && driver.canNotGiveOrTake(sender.getName())) {
            PrimPlugin.send(sender, config.getString("errors.canNotGive"));
            return true;
        }

        if(action.equals("-") && driver.canNotGiveOrTake(sender.getName())) {
            PrimPlugin.send(sender, config.getString("errors.canNotTake"));
            return true;
        }

        if(Utils.equalsOne(action, "+", "-")) {
            driver.updateReputation(player, value+count);
            driver.updateLastGiveOrTake(sender.getName());
        }

        if(action.equals("+"))
            PrimPlugin.send(sender, Utils.parse(config.getString("messages.give"), placeholders));

        if(action.equals("-"))
            PrimPlugin.send(sender, Utils.parse(config.getString("messages.take"), placeholders));

        placeholders.add(new Placeholder("count", count));
        if(action.equals("set")) {
            driver.updateReputation(player, count);
            PrimPlugin.send(sender, Utils.parse(config.getString("messages.set"), placeholders));
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        int argNumber = Math.max(args.length-1, 0);
        if(argNumber == 0) {
            List<String> output = new ArrayList<>();
            output.add("top");      // Топ игроков по репутации
            output.add("get");      // Получить свою репутацию / репутацию другого игрока
            output.add("+");        // +1 репутация другому игроку
            output.add("-");        // -1 репутация другому игроку
            if(sender.hasPermission("primplugin.commands.rep.set"))
                output.add("set");  // Установить репутацию любого игрока

            return output;
        }

        if(argNumber == 1 && !args[0].equals("top"))
            return PrimPlugin.getOnlinePlayersNames();

        if(argNumber == 2 && args[0].equals("set"))
            return List.of("1", "2", "4", "8");

        return List.of();
    }

    public void updateConfig(Config config) {
        this.config = config;
    }
}
