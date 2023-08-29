package ru.primland.plugin.modules.recipes;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.argument.type.StringArgument;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandInfo(
        name="create",
        description="Воссоздать результат указанного рецепта",
        permission="primplugin.commands.recipes.create",
        parent="recipes"
)
public class CreateCommand extends Command {
    private static final Pattern namespacePattern = Pattern.compile("(?:[^:]+:)?(.+)");

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        addArgument(new StringArgument("recipe", "ID рецепта", true, (ctx) -> getRecipes(true)));
    }

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {}

    /**
     * Выполнить команду с указанными данными
     *
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    @Override
    public @Nullable String execute(@NotNull CommandContext ctx) {
        String recipe = Objects.requireNonNull(ctx.get("recipe"));
        if(!getRecipes(true).contains(recipe) && !getRecipes(false).contains(recipe))
            return Utils.parse(CustomRecipes.config.getString("invalidRecipe"), new Placeholder("recipe", recipe));

        NamespacedKey key = getKeyByStringKey(recipe);
        if(key == null)
            return PrimPlugin.i18n.getString("internalError");

        ItemStack result = Objects.requireNonNull(Bukkit.getServer().getRecipe(key)).getResult();

        // TODO: заменить на Utils#give
        if(Arrays.asList(ctx.sender.getInventory().getStorageContents()).contains(null)) {
            ctx.sender.getInventory().addItem(result);
        } else Utils.dropItem(ctx.sender, result);

        return Utils.parse(CustomRecipes.config.getString("commandDone"), new Placeholder("recipe", recipe));
    }

    /**
     * Получить список с рецептами плагина
     *
     * @param namespace Должно ли пространство имён содержаться в ID рецептов
     * @return Список строк
     */
    private @NotNull List<String> getRecipes(boolean namespace) {
        List<String> keys = new ArrayList<>();
        CustomRecipes.registeredRecipes.forEach(recipe -> keys.add(namespace ? recipe.toString() : recipe.getKey()));
        return keys;
    }

    /**
     * Получить ключ пространства имён используя его копию в виде строки
     *
     * @param key Ключ-строк
     * @return {@link NamespacedKey}
     */
    private @Nullable NamespacedKey getKeyByStringKey(String key) {
        Matcher matcher = namespacePattern.matcher(key);
        if(!matcher.matches()) return null;
        return new NamespacedKey(PrimPlugin.instance, matcher.group(1));
    }
}
