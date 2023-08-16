package ru.primland.plugin.modules.cards;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.plugin.IPluginCommand;
import ru.primland.plugin.utils.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class GetCardCommand implements IPluginCommand {
    private final Config config;

    public GetCardCommand(Config cardsConfig) {
        this.config = cardsConfig;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull List<String> args) {
        if(args.size() == 0) {
            PrimPlugin.send(sender, Utils.translate(config.getString("cards.commandErrors.specifyRarity")));
            return;
        }

        String rarityID = args.get(0);
        if(!getRarities().contains(rarityID)) {
            PrimPlugin.send(sender, Utils.translate(config.getString("cards.commandErrors.invalidRarity")));
            return;
        }

        if(args.size() == 1) {
            PrimPlugin.send(sender, Utils.translate(config.getString("cards.commandErrors.specifyCardID")));
            return;
        }

        String cardID = args.get(1);
        if(!getCardIDs(rarityID).contains(cardID)) {
            PrimPlugin.send(sender, Utils.translate(config.getString("cards.commandErrors.invalidCardID")));
            return;
        }

        AtomicReference<Map<?, ?>> rarity = new AtomicReference<>(new HashMap<>());
        config.getMapList("rarityList").forEach(rarityE -> {
            if(!rarityE.get("id").toString().equals(rarityID)) return;
            rarity.set(rarityE);
        });

        AtomicReference<Map<?, ?>> card = new AtomicReference<>(new HashMap<>());
        config.getMapList("cards.list").forEach(cardE -> {
            if(!cardE.get("rarity").toString().equals(rarityID)) return;
            if(!cardE.get("id").toString().equals(cardID)) return;
            card.set(cardE);
        });

        ItemStack item = CollectibleCards.getCard(config, rarity.get(), card.get());
        if(item == null) return;

        Player player = (Player) sender;
        if(Arrays.asList(player.getInventory().getStorageContents()).contains(null)) {
            player.getInventory().addItem(item);
        } else Utils.dropItem(player, item);

        PrimPlugin.send(sender, Utils.parse(config.getString("cards.commandDone"),
                new Placeholder("player", player.getName()),
                new Placeholder("card", card.get().get("name")),
                new Placeholder("cardID", cardID),
                new Placeholder("rarity.color", rarity.get().get("color")),
                new Placeholder("rarity.name", rarity.get().get("name"))));
    }

    private @NotNull List<String> getRarities() {
        List<java.lang.String> cardRarities = new ArrayList<>();
        config.getMapList("rarityList").forEach(rarity -> cardRarities.add(rarity.get("id").toString()));
        return cardRarities;
    }

    private @NotNull List<String> getCardIDs(String rarity) {
        List<String> cardIDs = new ArrayList<>();
        config.getMapList("cards.list").forEach(card -> {
            if(!card.get("rarity").toString().equals(rarity)) return;
            cardIDs.add(card.get("id").toString());
        });

        return cardIDs;
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if(Math.max(args.length-1, 0) == 2)
            return getRarities();

        if(Math.max(args.length-1, 0) == 3)
            return getCardIDs(args[2]);

        return List.of();
    }

    @Override
    public String getName() {
        return "get";
    }

    @Override
    public String getDescription() {
        return "Выдаёт Вам указанную карточку";
    }

    @Override
    public List<String> getRequiredPermissions() {
        return List.of("primplugin.commands.cards.get");
    }

    @Override
    public String getUsage() {
        return "{редкость} {ID карточки}";
    }
}
