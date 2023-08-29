package ru.primland.plugin.modules.cards;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import net.minecraft.nbt.StringTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.modules.manager.Module;
import ru.primland.plugin.modules.manager.ModuleInfo;
import ru.primland.plugin.utils.NBTUtils;
import ru.primland.plugin.utils.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ModuleInfo(name="cards", config="cards", description="Коллекционные карточки")
public class CollectibleCards extends Module implements Listener {
    // TODO: Переписать... полностью
    
    private final static Pattern actionPattern = Pattern.compile("\\[([a-zA-Z0-9_]+)] *(.*)?");

    public static Config config;
    private List<String> boxSignature;
    private List<String> cardSignature;
    private List<Map<?, ?>> cards;
    private Map<String, Map<?, ?>> rarities;

    private List<String> players;

    /**
     * Загрузить (включить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        config = getConfig();
        if(config == null)
            return;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Получаем сигнатуры
        this.boxSignature = NBTUtils.getSignature(config.getString("box.signature", "prp_type=cards_box"));
        this.cardSignature = NBTUtils.getSignature(config.getString("cards.signature", "prp_type=card:{rarity}:{id}"));

        // Получаем список карточек
        this.cards = config.getMapList("cards.list");

        // Получаем список редкостей
        this.rarities = new HashMap<>();
        config.getMapList("rarityList").forEach(rarity -> rarities.put(((String) rarity.get("id")), rarity));

        // Инициализируем список игроков
        this.players = new ArrayList<>();
    }

    /**
     * Отгрузить (выключить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        PlayerSwapHandItemsEvent.getHandlerList().unregister(this);
        CraftItemEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
    }

    /**
     * Обработать событие... связанное с предметами?
     *
     * @param item   Объект предмета
     * @param player Объект игрока
     * @param event  Объект события
     * @param error  Ключ локализации ошибки
     */
    private void handleItemEvent(ItemStack item, Player player, Cancellable event, String error) {
        Material material = Material.valueOf(config.getString("box.material", "ENCHANTED_BOOK"));
        if(item == null || !item.getType().equals(material) || NBTUtils.isInvalidSignature(this.boxSignature, item))
            return;

        event.setCancelled(true);
        PrimPlugin.send(player, Utils.translate(config.getString(error, "debug")));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDropItem(@NotNull PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if(!players.contains(player.getName()))
            return;

        handleItemEvent(event.getItemDrop().getItemStack(), player, event, "box.dropError");
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(!players.contains(player.getName()))
            return;

        handleItemEvent(event.getCurrentItem(), player, event, "box.moveError");
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(!players.contains(player.getName()))
            return;

        handleItemEvent(event.getOldCursor(), player, event, "box.moveError");
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHandSwap(@NotNull PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if(!players.contains(player.getName()))
            return;

        // Проверяем обе руки, если в 1 их них есть коробка, то отменяем и выдаём ошибку
        handleItemEvent(event.getMainHandItem(), player, event, "box.moveError");
        handleItemEvent(event.getOffHandItem(), player, event, "box.moveError");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(@NotNull CraftItemEvent event) {
        Material material = Material.valueOf(config.getString("cards.material", "PAPER"));
        List<String> signature = new ArrayList<>();
        signature.add(this.cardSignature.get(0));
        signature.add(Utils.parse(this.cardSignature.get(1),
                new Placeholder("rarity", "[a-z0-9_]+"),
                new Placeholder("id", "[a-zA-Z0-9_]+")));

        event.getViewers().forEach(viewer -> {
            if(!(viewer instanceof Player))
                return;

            ItemStack[] items = event.getInventory().getMatrix();
            for(int i = 0; i < 9; i++) {
                if(items.length <= i)
                    break;

                ItemStack item = items[i];
                if(item == null)
                    continue;

                if(!item.getType().equals(material) || NBTUtils.isInvalidSignature(signature, item))
                    continue;

                if(event.isCancelled())  // Нужно, чтобы сообщение не писало по 2-3+ раз
                    return;

                event.setCancelled(true);
                PrimPlugin.send(viewer, Utils.translate(config.getString("cards.craftError")));
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Material material = Material.valueOf(config.getString("cards.material", "PAPER"));
        if(!item.getType().equals(material) || NBTUtils.isInvalidSignature(this.cardSignature, item))
            return;

        event.setCancelled(true);
        PrimPlugin.send(event.getPlayer(), Utils.translate(config.getString("cards.placeError")));
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        Action action = event.getAction();
        if(!(action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)))
            return;

        ItemStack item = event.getItem();
        Material material = Material.valueOf(config.getString("box.material", "ENCHANTED_BOOK"));
        if(item == null || !item.getType().equals(material) || NBTUtils.isInvalidSignature(this.boxSignature, item))
            return;

        Player player = event.getPlayer();
        if(players.contains(player.getName())) {
            PrimPlugin.send(player, Utils.translate(config.getString("box.openError")));
            return;
        }

        players.add(player.getName());

        Bukkit.getScheduler().runTaskAsynchronously(PrimPlugin.instance, () -> {
            config.getStringList("box.preOpenAction").forEach(poAction -> {
                Matcher matcher = actionPattern.matcher(poAction);
                if(!matcher.matches())
                    return;

                String actionType = matcher.group(1);
                String actionData = matcher.group(2);
                switch(actionType) {
                    case "SOUND" ->
                            Utils.playSound(player, actionData, SoundCategory.MUSIC);

                    case "TITLE" -> {
                        boolean isSubtitle = actionData.startsWith(":subtitle:");
                        if(!isSubtitle) {
                            player.sendTitle(actionData, null, 10, 70, 20);
                            break;
                        }

                        player.sendTitle(" ", actionData.replace(":subtitle:", ""), 10, 70, 20);
                    }

                    case "SLEEP" -> {
                        try {
                            Thread.sleep(Long.parseLong(actionData));
                        } catch(InterruptedException ignore) {}
                    }
                }
            });

            List<ItemStack> giveCards = new ArrayList<>();
            List<String> giveCardNames = new ArrayList<>();
            for(int i = 0; i < config.getInteger("box.dropCards", 3); i++) {
                Map<Map<?, ?>, ItemStack> card = randomCard();
                Map<?, ?> cardData = card.keySet().toArray(Map<?, ?>[]::new)[0];
                Map<?, ?> rarity = rarities.get((String) cardData.get("rarity"));
                String cardName = (String) cardData.get("name");

                giveCards.add(card.get(cardData));
                giveCardNames.add(Utils.parse(config.getString("box.cardFormat", "{name}"),
                        new Placeholder("name", cardName),
                        new Placeholder("id", cardData.get("id")),
                        new Placeholder("rarity.color", rarity.get("color")),
                        new Placeholder("rarity.name", rarity.get("name"))));
            }

            item.setAmount(item.getAmount()-1);
            players.remove(player.getName());

            giveCards.forEach(card -> Utils.give(player, card));
            PrimPlugin.send(player, Utils.parse(config.getString("box.message"),
                    new Placeholder("cards", String.join(", ", giveCardNames))));
        });
    }

    /**
     * Выбрать и получить информацию об 1 рандомной карточке
     * @return Карта с ключами карточки
     */
    public Map<?, ?> getOneCard() {
        AtomicReference<Map<?, ?>> output = new AtomicReference<>(null);
        this.rarities.forEach((id, rarity) -> {
            if(output.get() != null)
                return;

            if(Utils.randomInt(1, 100) > (Integer) rarity.get("rarity"))
                return;

            List<Map<?, ?>> aCards = new ArrayList<>();
            this.cards.forEach(card -> {
                if(!card.get("rarity").equals(id))
                    return;

                aCards.add(card);
            });

            if(aCards.isEmpty())
                return;

            output.set(aCards.get(Utils.randomInt(0, aCards.size()-1)));
        });

        return output.get() == null ? getOneCard() : output.get();
    }

    /**
     * Получить карточку как предмет
     *
     * @param rarity Информация об редкости карточки
     * @param card   Информация об самой карточке
     * @return {@link ItemStack}, если всё прошло успешно, иначе null
     */
    public static @Nullable ItemStack getCard(Map<?, ?> rarity, Map<?, ?> card) {
        ItemStack item = new ItemStack(Material.valueOf(config.getString("cards.material", "PAPER")));
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return null;

        Placeholder[] placeholders = {
                new Placeholder("name", card.get("name")),
                new Placeholder("id", card.get("id")),
                new Placeholder("rarity.color", rarity.get("color")),
                new Placeholder("rarity.name", rarity.get("name"))
        };

        meta.setDisplayName(Utils.parse(config.getString("cards.format.displayName"), placeholders));

        List<String> lore = new ArrayList<>();
        config.getMapList("cards.format.lore").forEach(line -> {
            String type = String.valueOf(line.get("type"));
            String text = String.valueOf(line.get("text"));
            if(type.equals("line"))
                lore.add(Utils.parse(text, placeholders));

            if(type.equals("template"))
                Utils.convertObjectToList(card.get("lore")).forEach(original -> {
                    Placeholder[] tPlaceholders = placeholders.clone();
                    List<Placeholder> placeholderList = new ArrayList<>(List.of(tPlaceholders));
                    placeholderList.add(new Placeholder("original", original));
                    lore.add(Utils.parse(text, placeholderList.toArray(Placeholder[]::new)));
                });
        });

        meta.setLore(lore);
        item.setItemMeta(meta);

        List<String> signature = NBTUtils.getSignature(config.getString("cards.signature", "prp_type=card:{rarity}:{id}"));
        if(signature == null) return null;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        nmsItem.addTagElement(signature.get(0), StringTag.valueOf(Utils.parse(signature.get(1),
                new Placeholder("rarity", card.get("rarity")),
                new Placeholder("id", card.get("id")))));

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    /**
     * Получить рандомную карточку
     * @return Карта, где ключ - это сырая информация о карточке, а значение - предмет
     */
    public Map<Map<?, ?>, ItemStack> randomCard() {
        Map<?, ?> card = getOneCard();
        Map<?, ?> rarity = rarities.get((String) card.get("rarity"));

        ItemStack item = getCard(rarity, card);
        if(item == null)
            return null;

        return Map.of(card, item);
    }
}
