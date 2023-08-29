package ru.primland.plugin.modules.cards;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import net.minecraft.nbt.StringTag;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
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
import ru.primland.plugin.commands.manager.argument.type.IntegerArgument;
import ru.primland.plugin.commands.manager.argument.type.PlayerArgument;
import ru.primland.plugin.utils.NBTUtils;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(
        name="give-box",
        description="Выдать указанному игроку коробки с карточками",
        permission="primplugin.commands.cards.give_box",
        parent="cards"
)
public class GiveBoxCommand extends Command {
    private Config config;

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        config = CollectibleCards.config;
        addArgument(new PlayerArgument<Player>("player", "игрок", false, false));
        addArgument(new IntegerArgument("amount", "количество", false));
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
        Player player = ctx.get("player");
        if(player == null)
            player = ctx.sender;

        Integer ctxAmount = ctx.get("amount");
        int amount = ctxAmount == null ? 1 : ctxAmount;

        giveBox(player, amount);
        return Utils.parse(config.getString("box.commandDone"), new Placeholder("player", player.getName()));
    }

    /**
     * Получить коробку с карточками в качестве предмета
     *
     * @param amount Количество коробок
     * @return {@link ItemStack}
     */
    public static @NotNull ItemStack getBox(int amount) {
        Material material = Material.valueOf(CollectibleCards.config.getString("box.material", "ENCHANTED_BOOK"));
        ItemStack item = new ItemStack(material, Math.min(amount, material.getMaxStackSize()));

        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(Utils.translate(CollectibleCards.config.getString("box.displayName")));

            List<String> lore = new ArrayList<>();
            CollectibleCards.config.getStringList("box.lore").forEach(line -> lore.add(Utils.translate(line)));
            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        List<String> signature = NBTUtils.getSignature(CollectibleCards.config.getString("box.signature", "prp_type=cards_box"));
        if(signature == null) return item;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        nmsItem.addTagElement(signature.get(0), StringTag.valueOf(signature.get(1)));
        item = CraftItemStack.asBukkitCopy(nmsItem);
        return item;
    }

    /**
     * Выдать игроку коробку с карточками
     *
     * @param player Объект игрока
     * @param amount Количество коробок
     */
    public static void giveBox(Player player, int amount) {
        ItemStack item = getBox(amount);
        for(int i = 0; i < (amount > item.getType().getMaxStackSize() ? amount : 1); i++)
            Utils.give(player, item);
    }
}
