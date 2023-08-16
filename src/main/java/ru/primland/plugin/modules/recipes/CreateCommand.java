package ru.primland.plugin.modules.recipes;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.plugin.IPluginCommand;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCommand implements IPluginCommand {
    private static final Pattern namespacePattern = Pattern.compile("(?:[^:]+:)?(.+)");
    private final Config config;

    public CreateCommand(Config cardsConfig) {
        this.config = cardsConfig;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull List<String> args) {
        if(args.size() == 0) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.specifyRecipe")));
            return;
        }

        String recipe = args.get(0);
        if(!getRecipes().contains(recipe) && !getRecipes(true).contains(recipe)) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.invalidRecipe"),
                    new Placeholder("recipe", recipe)));

            return;
        }

        NamespacedKey key = getKeyByStringKey(recipe);
        if(key == null) {
            PrimPlugin.send(sender, Utils.parse(config.getString("errors.pluginSideError")));
            return;
        }

        ItemStack result = Objects.requireNonNull(Bukkit.getServer().getRecipe(key)).getResult();

        Player player = (Player) sender;
        if(Arrays.asList(player.getInventory().getStorageContents()).contains(null)) {
            player.getInventory().addItem(result);
        } else Utils.dropItem(player, result);

        PrimPlugin.send(sender, Utils.parse(config.getString("commandDone"),
                new Placeholder("recipe", recipe)));
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if(Math.max(args.length-1, 0) == 2)
            return getRecipes();

        return null;
    }

    private @NotNull List<String> getRecipes() {
        return getRecipes(false);
    }

    private @NotNull List<String> getRecipes(boolean withoutNamespace) {
        List<String> keys = new ArrayList<>();
        CustomRecipes.getRegisteredRecipes().forEach(recipe -> keys.add(withoutNamespace ? recipe.getKey()
                : recipe.toString()));

        return keys;
    }

    private @Nullable NamespacedKey getKeyByStringKey(String key) {
        Matcher matcher = namespacePattern.matcher(key);
        if(!matcher.matches()) return null;
        return new NamespacedKey(PrimPlugin.getInstance(), matcher.group(1));
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Создаёт и выдаёт Вам результат указанного рецепта";
    }

    @Override
    public List<String> getRequiredPermissions() {
        return List.of("primplugin.commands.recipes.create");
    }

    @Override
    public String getUsage() {
        return "{id рецепта}";
    }
}
