package ru.primland.plugin.modules.cards;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.argument.type.PlayerArgument;
import ru.primland.plugin.commands.manager.argument.type.StringArgument;
import ru.primland.plugin.utils.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@CommandInfo(
        name="give-card",
        description="Выдать данному игроку указанную карточку",
        permission="primplugin.commands.cards.give_box",
        parent="cards"
)
public class GiveCardCommand extends Command {
    private Config config;

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        config = CollectibleCards.config;
        addArgument(new StringArgument("rarity", "редкость", true, (ctx) -> getRarities()));
        addArgument(new StringArgument("card", "ID карточки", true, (ctx) -> getCardIDs(ctx.get("rarity"))));
        addArgument(new PlayerArgument<Player>("player", "игрок", false, false));
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
        String rarityID = ctx.get("rarity");
        if(!getRarities().contains(rarityID))
            return Utils.parse(config.getString("cards.commandErrors.invalidRarity"));

        String cardID = ctx.get("card");
        if(!getCardIDs(rarityID).contains(cardID))
            return Utils.parse(config.getString("cards.commandErrors.invalidCardID"));

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

        ItemStack item = CollectibleCards.getCard(rarity.get(), card.get());
        if(item == null)
            return null;

        Utils.give(ctx.sender, item);

        return Utils.parse(
                config.getString("cards.commandDone"),
                new Placeholder("player", ctx.sender.getName()),
                new Placeholder("card", card.get().get("name")),
                new Placeholder("cardID", cardID),
                new Placeholder("rarity.color", rarity.get().get("color")),
                new Placeholder("rarity.name", rarity.get().get("name"))
        );
    }

    /**
     * Получить список доступных редкостей карточек
     * @return Список строк
     */
    private @NotNull List<String> getRarities() {
        List<String> cardRarities = new ArrayList<>();
        config.getMapList("rarityList").forEach(rarity -> cardRarities.add(rarity.get("id").toString()));
        return cardRarities;
    }

    /**
     * Получить ID карточек с указанной редкостью
     *
     * @param rarity Редкость карточки
     * @return Список строк
     */
    private @NotNull List<String> getCardIDs(String rarity) {
        List<String> cardIDs = new ArrayList<>();
        config.getMapList("cards.list").forEach(card -> {
            if(!card.get("rarity").toString().equals(rarity)) return;
            cardIDs.add(card.get("id").toString());
        });

        return cardIDs;
    }
}
