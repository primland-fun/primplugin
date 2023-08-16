package ru.primland.plugin.modules.recipes;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.modules.IPluginModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomRecipes implements IPluginModule {
    private static final List<NamespacedKey> registeredRecipes = new ArrayList<>();

    private boolean enabled;
    private List<Map<?, ?>> recipes;
    private FoodListener listener;
    private ItemFramesFix fix;

    public static List<NamespacedKey> getRegisteredRecipes() {
        return registeredRecipes;
    }

    public static void registerRecipeLocally(NamespacedKey key) {
        registeredRecipes.add(key);
    }

    /**
     * Получает и возвращает название данного модуля
     * @return Название модуля
     */
    @Override
    public String getName() {
        return "recipes";
    }

    /**
     * Получает и возвращает название конфигурации данного модуля
     * @return Название модуля
     */
    @Override
    public String getConfigName() {
        return "recipes.yml";
    }

    /**
     * Получает и возвращает описание этого модуля
     * @return Описание модуля
     */
    @Override
    public String getDescription() {
        return "Кастомные рецепты для нашего сервера";
    }

    /**
     * Включает данный модуль
     * @param plugin Объект PrimPlugin
     */
    @Override
    public void enable(@NotNull PrimPlugin plugin) {
        Config config = plugin.getManager().getModuleConfig(getName());

        // Добавляем рецепты
        recipes = plugin.getManager().getModuleConfig(getName()).getMapList("recipes");

        String foodSignature = config.getString("foodSignature", "prp_type=food:{id}");
        recipes.forEach(rawRecipe -> {
            Recipe recipe = RecipeUtils.toRecipe(rawRecipe, foodSignature);
            if(recipe == null) return;
            Bukkit.addRecipe(recipe);
        });

        // Регистрируем слушатели
        listener = new FoodListener(config, recipes);
        listener.register(plugin);

        // TODO: fix = new ItemFramesFix();
        // fix.register(plugin);

        // Регистрируем команды
        plugin.getManager().registerCommandFor(getName(), new CreateCommand(
                PrimPlugin.getInstance().getManager().getModuleConfig(getName())));

        // Маркируем модуль как включённый
        enabled = true;
    }

    /**
     * Выключает этот модуль
     * @param plugin Объект PrimPlugin
     */
    @Override
    public void disable(@NotNull PrimPlugin plugin) {
        recipes.forEach(recipe -> Bukkit.getServer().removeRecipe(new NamespacedKey(plugin,
                "recipe." + recipe.get("id").toString())));

        // Отменяем регистрацию слушателей
        listener.unregister();
        //fix.unregister();

        // Удаляем из главной команды плагина команду recipes
        plugin.getManager().unregisterCommandsFor(getName());

        // Маркируем модуль как выключенный
        enabled = false;
    }

    /**
     * Включён ли модуль
     * @return Ответ на данный выше вопрос
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Немного информации о модуле плагина и его состоянии
     * @return Информация о модуле
     */
    @Override
    public String information() {
        return null;
    }
}
