package ru.primland.plugin.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.PrimPlugin;

import java.util.HashMap;
import java.util.function.Consumer;

public class CustomMenu implements Listener {
    private String title;
    @Setter private int size;
    private final HashMap<Integer, ItemStack> items;
    private final HashMap<Integer, Consumer<CustomMenu>> itemsCallbacks;

    @Getter private Inventory inventory;
    @Getter private Player player;

    public CustomMenu(String title, int size) {
        this.title = Utils.translate(title);
        this.size = size;
        this.items = new HashMap<>();
        this.itemsCallbacks = new HashMap<>();
    }

    public void setTitle(String title) {
        this.title = Utils.translate(title);
    }

    public void setItem(int slot, ItemStack item, Consumer<CustomMenu> callback) {
        items.put(slot, item);
        itemsCallbacks.put(slot, callback);
    }

    public void open(@NotNull Player player) {
        this.player = player;
        inventory = Bukkit.createInventory(null, size, title);
        items.forEach(inventory::setItem);
        player.openInventory(inventory);

        Bukkit.getPluginManager().registerEvents(this, PrimPlugin.instance);
    }

    public void close(boolean close) {
        if(close) this.player.closeInventory();
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }

    private void handleInventoryMove(@NotNull InventoryInteractEvent event, int slot) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if(!player.getName().equals(this.player.getName()))
            return;

        if(!inventory.equals(this.inventory))
            return;

        event.setCancelled(true);
        Consumer<CustomMenu> callback = itemsCallbacks.get(slot);
        if(callback == null)
            return;

        callback.accept(this);
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        handleInventoryMove(event, event.getSlot());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        handleInventoryMove(event, -1);
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        if(player.getUniqueId() != this.player.getUniqueId())
            return;

        if(inventory.getContents() != this.inventory.getContents())
            return;

        close(false);
    }
}
