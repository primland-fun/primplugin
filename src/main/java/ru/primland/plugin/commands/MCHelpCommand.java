package ru.primland.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.CustomMenu;
import ru.primland.plugin.utils.NBTUtils;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCHelpCommand implements CommandExecutor {
    private final static Pattern actionPattern = Pattern.compile("\\[([a-zA-Z0-9_]+)] *(.*)?");

    private Config config;

    public MCHelpCommand(Config config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof ConsoleCommandSender) {
            PrimPlugin.send(PrimPlugin.getInstance().getI18n().getString("playersOnly"));
            return true;
        }

        int size = config.getInteger("size", 9);
        CustomMenu menu = new CustomMenu(config.getString("title"), size);

        config.getMapList("items").forEach(item -> {
            int slot = (int) item.get("slot");
            if(slot > size-1) {
                PrimPlugin.send("&cСлот " + slot + " недопустим с указанным размером меню (minecraft_help.yml)");
                return;
            }

            Material type = Material.valueOf(item.get("type").toString());
            ItemStack itemStack = new ItemStack(type);
            if(type == Material.PLAYER_HEAD && item.containsKey("texture"))
                itemStack = NBTUtils.addTexture(itemStack, item.get("texture").toString());

            ItemMeta meta = itemStack.getItemMeta();
            if(meta == null) return;

            if(item.containsKey("displayName"))
                meta.setDisplayName(Utils.parse(item.get("displayName").toString()));

            if(item.containsKey("lore")) {
                List<String> lore = new ArrayList<>();
                Utils.convertObjectToList(item.get("lore")).forEach(line ->
                        lore.add(Utils.parse(line.toString())));

                meta.setLore(lore);
            }

            itemStack.setItemMeta(meta);

            if(item.containsKey("clickActions")) {
                List<?> actions = Utils.convertObjectToList(item.get("clickActions"));
                Consumer<CustomMenu> callback = (cmenu -> actions.forEach(action -> {
                    Player player = cmenu.getPlayer();
                    Matcher matcher = actionPattern.matcher((String) action);
                    if(!matcher.matches())
                        return;

                    String actionType = matcher.group(1);
                    String actionData = matcher.group(2);

                    if(actionType.equals("SOUND"))
                        Utils.playSound(player, actionData);

                    if(actionType.equals("MESSAGE") && actionData.length() == 0)
                        PrimPlugin.send("&cУкажите сообщение! (minecraft_help.yml)");

                    if(actionType.equals("MESSAGE") && actionData.length() > 0)
                        player.sendMessage(Utils.parse(actionData));

                    if(actionType.equals("PLAYER") && actionData.length() == 0)
                        PrimPlugin.send("&cУкажите команду (без /)! (minecraft_help.yml)");

                    if(actionType.equals("PLAYER") && actionData.length() > 0)
                        player.performCommand(actionData);

                    if(actionType.equals("CONSOLE") && actionData.length() == 0)
                        PrimPlugin.send("&cУкажите команду (без /)! (minecraft_help.yml)");

                    if(actionType.equals("CONSOLE") && actionData.length() > 0)
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), actionData);
                    
                    if(actionType.equals("CLOSE"))
                        cmenu.close(true);
                }));

                menu.setItem((int) item.get("slot"), itemStack, callback);
                return;
            }

            menu.setItem((int) item.get("slot"), itemStack, null);
        });

        menu.open((Player) sender);
        return true;
    }

    public void updateConfig(Config config) {
        this.config = config;
    }
}
