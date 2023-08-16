package ru.primland.plugin.modules.cards;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import net.minecraft.nbt.StringTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.plugin.IPluginCommand;
import ru.primland.plugin.utils.NBTUtils;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GiveBoxCommand implements IPluginCommand {
    private final Config config;

    public GiveBoxCommand(Config cardsConfig) {
        this.config = cardsConfig;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull List<String> args) {
        if(args.size() == 0) {
            PrimPlugin.send(sender, Utils.translate(config.getString("box.commandErrors.specifyPlayer")));
            return;
        }

        Player player = Bukkit.getPlayer(args.get(0));
        if(player == null) {
            PrimPlugin.send(sender, Utils.parse(config.getString("box.commandErrors.playerNotFound"),
                    new Placeholder("name", args.get(0))));
            return;
        }

        int amount = 1;
        if(args.size() >= 2)
            amount = Integer.parseInt(args.get(1));

        giveBox(player, amount, config);
        PrimPlugin.send(sender, Utils.parse(config.getString("box.commandDone"),
                new Placeholder("player", player.getName())));
    }

    public static @Nullable ItemStack getBox(@NotNull Config config, int amount) {
        Material material = Material.valueOf(config.getString("box.material", "ENCHANTED_BOOK"));
        ItemStack item = new ItemStack(material, Math.min(amount, material.getMaxStackSize()));

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(Utils.translate(config.getString("box.displayName")));

        List<String> lore = new ArrayList<>();
        config.getStringList("box.lore").forEach(line -> lore.add(Utils.translate(line)));
        meta.setLore(lore);

        item.setItemMeta(meta);

        List<String> signature = NBTUtils.getSignature(config.getString("box.signature", "prp_type=cards_box"));
        if(signature == null) return null;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        nmsItem.addTagElement(signature.get(0), StringTag.valueOf(signature.get(1)));
        item = CraftItemStack.asBukkitCopy(nmsItem);
        return item;
    }

    public static void giveBox(Player player, int amount, @NotNull Config config) {
        ItemStack item = getBox(config, amount);
        if(item == null) return;

        for(int i = 0; i < (amount > item.getType().getMaxStackSize() ? amount : 1); i++) {
            if(Arrays.asList(player.getInventory().getStorageContents()).contains(null)) {
                player.getInventory().addItem(item);
                continue;
            }

            Utils.dropItem(player, item);
        }
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if(Math.max(args.length-1, 0) == 2) {
            List<String> playerNames = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> playerNames.add(player.getName()));
            return playerNames;
        }

        if(Math.max(args.length-1, 0) == 3)
            return List.of("1", "2", "4", "8", "16", "32", "64");

        return null;
    }

    @Override
    public String getName() {
        return "give-box";
    }

    @Override
    public String getDescription() {
        return "Выдаёт указанному игроку указанное количество ящиков с карточками";
    }

    @Override
    public List<String> getRequiredPermissions() {
        return List.of("primplugin.commands.cards.giveBox");
    }

    @Override
    public String getUsage() {
        return "{игрок} [количество]";
    }
}
