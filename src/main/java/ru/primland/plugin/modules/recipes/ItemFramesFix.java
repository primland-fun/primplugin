package ru.primland.plugin.modules.recipes;

import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.PrimPlugin;

public class ItemFramesFix implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(@NotNull EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof ItemFrame))
            return;

        ItemFrame frame = (ItemFrame) event.getEntity();
        PrimPlugin.send(frame.toString());
    }

    public void register(@NotNull PrimPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }
}
