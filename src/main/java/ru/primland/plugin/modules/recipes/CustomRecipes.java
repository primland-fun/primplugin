package ru.primland.plugin.modules.recipes;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.modules.manager.Module;
import ru.primland.plugin.modules.manager.ModuleInfo;
import ru.primland.plugin.modules.manager.ModuleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ModuleInfo(name="recipes", config="recipes", description="Кастомные рецепты для нашего сервера")
public class CustomRecipes extends Module {
    // TODO: Полностью переписать
    public static final List<NamespacedKey> registeredRecipes = new ArrayList<>();
    public static Config config;

    private List<Map<?, ?>> recipes;
    private FoodListener listener;

    /**
     * Загрузить (включить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        config = getConfig();
        if(config == null)
            ModuleManager.disable("recipes");

        // Добавляем рецепты
        recipes = config.getMapList("recipes");

        String foodSignature = config.getString("foodSignature", "prp_type=food:{id}");
        recipes.forEach(rawRecipe -> {
            Recipe recipe = RecipeUtils.toRecipe(rawRecipe, foodSignature);
            if(recipe == null)
                return;

            Bukkit.addRecipe(recipe);
        });

        // Регистрируем слушатели
        listener = new FoodListener(config, recipes);
        listener.register(plugin);
    }

    /**
     * Отгрузить (выключить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {
        recipes.forEach(recipe -> Bukkit.getServer().removeRecipe(new NamespacedKey(plugin,
                "recipe." + recipe.get("id").toString())));

        // Отменяем регистрацию слушателей
        listener.unregister();
    }

    /**
     * Зарегистрировать рецепт локально
     *
     * @param key Ключ рецепта
     */
    public static void registerRecipeLocally(NamespacedKey key) {
        registeredRecipes.add(key);
    }
}
