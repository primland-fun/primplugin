package ru.primland.plugin.modules.recipes;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.NBTUtils;
import ru.primland.plugin.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FoodListener implements Listener {
    private final Config config;
    private final List<Map<?, ?>> recipes;
    
    public FoodListener(Config config, List<Map<?, ?>> recipes) {
        this.config = config;
        this.recipes = recipes;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEat(@NotNull PlayerItemConsumeEvent event) {
        List<String> signature = NBTUtils.getSignature(config.getString("foodSignature", "prp_type=food:{id}")
                .replace("{id}", "([a-zA-Z0-9_]+)"));

        ItemStack item = event.getItem();
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        CompoundTag nbt = nmsItem.getTag();
        if(nbt == null || NBTUtils.isInvalidSignature(signature, nmsItem))
            return;

        Player player = event.getPlayer();

        Matcher matcher = Pattern.compile(signature.get(1)).matcher(nbt.getString(signature.get(0)));
        if(!matcher.matches()) return;
        String foodId = matcher.group(1);

        recipes.forEach(recipe -> {
            if(!recipe.get("type").toString().equals("food"))
                return;

            if(!recipe.get("id").toString().equals(foodId))
                return;

            event.setCancelled(true);
            Map<?, ?> foodParameters = Utils.convertObjectToMap(Utils.convertObjectToMap(
                    recipe.get("result")).get("foodParameters"));

            if(foodParameters.containsKey("regenerateHP")) {
                AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                double maxHealth = attribute == null ? 20.0 : attribute.getValue();
                player.setHealth(Math.min(player.getHealth() + ((double)
                        foodParameters.get("regenerateHP")), maxHealth));
            }

            if(foodParameters.containsKey("regenerateSatiety"))
                player.setFoodLevel(player.getFoodLevel() + (int) foodParameters.get("regenerateSatiety"));

            if(foodParameters.containsKey("regenerateSaturation"))
                player.setSaturation(player.getSaturation() + ((Double) foodParameters
                        .get("regenerateSaturation")).floatValue());

            if(foodParameters.containsKey("clearEffects") && Boolean.parseBoolean(
                    foodParameters.get("clearEffects").toString())) {
                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            }

            if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR))
                player.getInventory().getItemInMainHand().setAmount(item.getAmount()-1);
        });
    }

    public void register(@NotNull PrimPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        PlayerItemConsumeEvent.getHandlerList().unregister(this);
    }
}
