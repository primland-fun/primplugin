package ru.primland.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.utils.CustomMenu;
import ru.primland.plugin.utils.NBTUtils;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandInfo(
        name="help",
        description="Справка по механикам сервера",
        aliases={"h"},
        playersOnly=true
)
public class HelpCommand extends Command {
    private final static Pattern actionPattern = Pattern.compile("\\[([a-zA-Z0-9_]+)] *(.*)?");

    public Config config;

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        config = Config.load("commands/minecraft_help.yml");
    }

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {
        config = null;
    }

    /**
     * Выполнить команду с указанными данными
     *
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    @Override
    public @Nullable String execute(@NotNull CommandContext ctx) {
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
            if(meta != null && item.containsKey("displayName"))
                meta.setDisplayName(Utils.parse(item.get("displayName").toString()));

            if(meta != null && item.containsKey("lore")) {
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
                        Utils.playSound(player, actionData, SoundCategory.BLOCKS);

                    if(actionType.equals("MESSAGE") && actionData.isEmpty())
                        PrimPlugin.send("&cУкажите сообщение! (minecraft_help.yml)");

                    if(actionType.equals("MESSAGE") && !actionData.isEmpty())
                        player.sendMessage(Utils.parse(actionData));

                    if(actionType.equals("PLAYER") && actionData.isEmpty())
                        PrimPlugin.send("&cУкажите команду (без /)! (minecraft_help.yml)");

                    if(actionType.equals("PLAYER") && !actionData.isEmpty())
                        player.performCommand(actionData);

                    if(actionType.equals("CONSOLE") && actionData.isEmpty())
                        PrimPlugin.send("&cУкажите команду (без /)! (minecraft_help.yml)");

                    if(actionType.equals("CONSOLE") && !actionData.isEmpty())
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), actionData);

                    if(actionType.equals("CLOSE"))
                        cmenu.close(true);
                }));

                menu.setItem((int) item.get("slot"), itemStack, callback);
                return;
            }

            menu.setItem((int) item.get("slot"), itemStack, null);
        });

        ctx.open(menu);
        return null;
    }
}
