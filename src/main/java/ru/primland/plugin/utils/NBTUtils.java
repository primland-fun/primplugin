package ru.primland.plugin.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.nbt.*;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.primland.plugin.PrimPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NBTUtils {
    private final static Pattern nbtPattern = Pattern.compile("([a-zA-Z0-9_\\-]+)=(.+)");

    public static @Nullable @Unmodifiable List<String> getSignature(String original) {
        Matcher matcher = nbtPattern.matcher(original);
        if(!matcher.matches())
            return null;

        return List.of(matcher.group(1), matcher.group(2));
    }

    public static boolean isInvalidSignature(List<String> signature, ItemStack item) {
        return isInvalidSignature(signature, CraftItemStack.asNMSCopy(item));
    }

    public static boolean isInvalidSignature(List<String> signature, net.minecraft.world.item.ItemStack item) {
        if(signature == null)
            return true;

        CompoundTag tag = item.getTag();
        if(tag == null || !tag.contains(signature.get(0)))
            return true;

        String value = tag.getString(signature.get(0));
        return !Pattern.compile(signature.get(1)).matcher(value).matches();
    }

    public static @NotNull Map<String, Tag> toTagMap(@NotNull Map<?, ?> nbt) {
        Map<String, Tag> output = new HashMap<>();
        nbt.forEach((key, value) -> {
            if(value instanceof String)
                output.put(key.toString(), StringTag.valueOf(value.toString()));

            if(value instanceof Integer)
                output.put(key.toString(), IntTag.valueOf((Integer) value));

            if(value instanceof Double)
                output.put(key.toString(), DoubleTag.valueOf((Double) value));

            if(value instanceof Map) {
                CompoundTag tag = new CompoundTag();
                toTagMap(Utils.convertObjectToMap(value)).forEach(tag::put);
                output.put(key.toString(), tag);
            }
        });

        return output;
    }

    public static @NotNull ItemStack modifyNBT(ItemStack original, String key, Tag tag) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(original);
        nmsItem.addTagElement(key, tag);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static @NotNull ItemStack modifyNBT(ItemStack original, Map<?, ?> tagMap) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(original);
        NBTUtils.toTagMap(tagMap).forEach(nmsItem::addTagElement);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static @NotNull ItemStack addTexture(@NotNull ItemStack original, @NotNull String texture) {
        ItemStack copy = original.clone();
        SkullMeta skull = Objects.requireNonNull((SkullMeta) copy.getItemMeta());

        UUID uuid = new UUID(texture.hashCode(), texture.hashCode());
        GameProfile profile = new GameProfile(uuid, "head");
        profile.getProperties().put("textures", new Property("textures", texture));

        try {
            Field profileField = skull.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skull, profile);
        } catch(NoSuchFieldException | IllegalAccessException error) {
            PrimPlugin.send("&cНе удалось установить текстуру головы через поля");
            error.printStackTrace();
        }

        copy.setItemMeta(skull);
        return copy;
    }
}
