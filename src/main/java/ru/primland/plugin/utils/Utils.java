package ru.primland.plugin.utils;

import io.github.stngularity.epsilon.engine.EpsilonEngine;
import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.PrimPlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static Pattern hexPattern = Pattern.compile("&#([0-9a-f]{6})");

    /**
     * Рандомизация числа
     * @param min Минимальное число
     * @param max Максимальное число
     * @return Случайное число
     */
    public static int randomInt(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    @Contract("_ -> new")
    public static @NotNull String translate(@NotNull String text) {
        StringBuilder buffer = new StringBuilder(text.length() + 4 * 8);

        Matcher matcher = hexPattern.matcher(text);
        while(matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, "&x&" + hex.charAt(0)
                    + "&" + hex.charAt(1) + "&" + hex.charAt(2) + "&" + hex.charAt(3)
                    + "&" + hex.charAt(4) + "&" + hex.charAt(5));
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static @NotNull String parse(String text, IPlaceholder... placeholders) {
        EpsilonEngine engine = new EpsilonEngine();
        engine.addPlaceholders(placeholders);
        return translate(engine.process(text));
    }

    public static @NotNull String parse(String text, @NotNull List<IPlaceholder> placeholders) {
        EpsilonEngine engine = new EpsilonEngine();
        engine.addPlaceholders(placeholders.toArray(IPlaceholder[]::new));
        return translate(engine.process(text));
    }

    public static List<?> convertObjectToList(Object object) {
        if(object == null)
            return null;

        if(object.getClass().isArray())
            return Arrays.asList((Object[]) object);

        if(object instanceof Collection)
            return new ArrayList<>((Collection<?>) object);

        return new ArrayList<>();
    }

    public static @NotNull Map<String, ?> convertObjectToMap(@NotNull Object object) {
        if(object instanceof Map)
            return new HashMap<>((Map<String, ?>) object);

        return new HashMap<>();
    }

    public static @NotNull Double evalMathString(String mathString) {
        Expression expression = new ExpressionBuilder(mathString).build();
        return expression.evaluate();
    }

    @Contract(pure = true)
    public static boolean equalsOne(Object value, Object @NotNull ... objects) {
        for(Object object : objects) {
            if(!value.equals(object)) continue;
            return true;
        }

        return false;
    }

    public static void playSound(Player player, String original) {
        if(original == null || original.equals("null"))
            return;

        String[] data = original.split(" ");
        if(data.length == 0) {
            PrimPlugin.send("&cУкажите название звука!");
            return;
        }

        float volume = 0.5f;
        if(data.length > 1) volume = Float.parseFloat(data[1]);

        Sound sound = Sound.valueOf(data[0]);
        player.playSound(player, sound, volume, 1f);
    }

    public static void dropItem(Player player, ItemStack item) {
        Bukkit.getScheduler().runTask(PrimPlugin.getInstance(), () ->
                player.getWorld().dropItemNaturally(player.getLocation(), item));
    }
}
