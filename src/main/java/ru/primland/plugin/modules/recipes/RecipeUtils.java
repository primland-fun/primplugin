package ru.primland.plugin.modules.recipes;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import net.minecraft.nbt.StringTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.NBTUtils;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeUtils {
    public static @Nullable Recipe toRecipe(@NotNull Map<?, ?> instruction, String foodSignature) {
        if(notContainsKeys(instruction, "type", "id", "recipe", "result")) {
            PrimPlugin.send("&cНе указан обязательный ключ (recipes.yml)");
            return null;
        }

        String type = instruction.get("type").toString();
        if(!type.equals("recipe") && !type.equals("food")) {
            PrimPlugin.send("&cНедопустимый тип рецепта (recipes.yml)");
            return null;
        }

        PrimPlugin plugin = PrimPlugin.getInstance();
        String id = instruction.get("id").toString();
        NamespacedKey key = new NamespacedKey(plugin, "recipe." + id);

        Map<?, ?> result = Utils.convertObjectToMap(instruction.get("result"));
        if(notContainsKeys(result, "material")) {
            PrimPlugin.send("&cУкажите result.material (recipes.yml)");
            return null;
        }

        ItemStack resultItem = new ItemStack(Material.valueOf(result.get("material").toString()));
        if(result.containsKey("amount"))
            resultItem.setAmount(Integer.parseUnsignedInt(result.get("amount").toString()));

        ItemMeta meta = resultItem.getItemMeta();
        if(meta == null) return null;

        if(result.containsKey("displayName"))
            meta.setDisplayName(Utils.translate(result.get("displayName").toString()));

        if(result.containsKey("lore")) {
            List<String> lore = new ArrayList<>();
            Utils.convertObjectToList(result.get("lore")).forEach(line -> lore.add(Utils.parse(line.toString())));
            meta.setLore(lore);
        }

        if(containsAndTrue(result, "unbreakable")) {
            meta.setUnbreakable(true);
            if(containsAndTrue(result, "hideUnbreakable"))
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        if(containsAndTrue(result, "glowEffect")) {
            meta.addEnchant(new GlowEnchantment(), 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if(result.containsKey("customModelData"))
            meta.setCustomModelData(Integer.parseInt(result.get("customModelData").toString()));

        resultItem.setItemMeta(meta);
        if(result.containsKey("customNBT"))
            resultItem = NBTUtils.modifyNBT(resultItem, Utils.convertObjectToMap(result.get("customNBT")));

        if(type.equals("food")) {
            List<String> signature = NBTUtils.getSignature(foodSignature);
            if(signature == null) return null;
            resultItem = NBTUtils.modifyNBT(resultItem, signature.get(0), StringTag.valueOf(
                    Utils.parse(signature.get(1), new Placeholder("id", id))));
        }

        Map<?, ?> recipe = Utils.convertObjectToMap(instruction.get("recipe"));
        String workplace = recipe.get("workplace").toString();
        if(!workplace.equals("workbench") && !workplace.equals("furnace")) {
            PrimPlugin.send("&cworkplace должен быть workbench или furnace (recipes.yml)");
            return null;
        }

        if(workplace.equals("workbench")) {
            List<?> grids = Utils.convertObjectToList(recipe.getOrDefault("grids", null));
            if(grids == null) {
                ShapelessRecipe output = new ShapelessRecipe(key, resultItem);
                Utils.convertObjectToList(recipe.get("ingredients")).forEach(rawIngredient ->
                        addIngredient(rawIngredient, output));

                CustomRecipes.registerRecipeLocally(key);
                return output;
            }

            ShapedRecipe output = new ShapedRecipe(key, resultItem);
            grids.forEach(grid -> {
                List<String> realGrid = new ArrayList<>();
                Utils.convertObjectToList(grid).forEach(line -> realGrid.add(line.toString()));
                output.shape(realGrid.toArray(String[]::new));
            });

            Utils.convertObjectToList(recipe.get("ingredients")).forEach(rawIngredient -> {
                Map<?, ?> ingredient = Utils.convertObjectToMap(rawIngredient);
                if(!ingredient.containsKey("letter")) {
                    PrimPlugin.send("&cУ одного из ингредиента рецепта " + key + " не указана буква (recipes.yml)");
                    return;
                }

                addIngredient(rawIngredient, output);
            });

            CustomRecipes.registerRecipeLocally(key);
            return output;
        }

        float exp = 0;
        if(result.containsKey("outputExperience"))
            exp = Float.parseFloat(result.get("outputExperience").toString());

        int cookingTime = 100;
        if(recipe.containsKey("cookingTime"))
            cookingTime = Integer.parseInt(recipe.get("cookingTime").toString());

        return initFurnaceRecipe(key, resultItem, recipe.get("ingredients"), exp, cookingTime);
    }

    @Contract(pure = true)
    public static boolean notContainsKeys(Map<?, ?> map, String @NotNull ... keys) {
        for(String key : keys) {
            if(map.containsKey(key)) continue;
            return true;
        }

        return false;
    }

    public static boolean containsAndTrue(@NotNull Map<?, ?> map, String key) {
        return map.containsKey(key) && Boolean.parseBoolean(map.get(key).toString());
    }

    public static boolean isNotMaterialDataIngredient(@NotNull Map<?, ?> rawData) {
        return !rawData.containsKey("amount") && !rawData.containsKey("ingredientNBT");
    }
    
    public static ItemStack toIngredient(@NotNull Map<?, ?> rawData) {
        Material material = Material.valueOf(rawData.get("material").toString());
        ItemStack ingredientItem = new ItemStack(material);

        if(rawData.containsKey("amount"))
            ingredientItem.setAmount(Integer.parseInt(rawData.get("amount").toString()));

        if(rawData.containsKey("ingredientNBT")) {
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(ingredientItem);
            Map<?, ?> customNBT = Utils.convertObjectToMap(rawData.get("customNBT"));
            NBTUtils.toTagMap(customNBT).forEach(nmsItem::addTagElement);
            ingredientItem = CraftItemStack.asBukkitCopy(nmsItem);
        }
        
        return ingredientItem;
    }

    public static void addIngredient(Object rawData, Recipe recipe) {
        Map<?, ?> ingredient = Utils.convertObjectToMap(rawData);

        ItemStack ingredientItem = toIngredient(ingredient);
        if(ingredient.containsKey("alternative")) {
            List<ItemStack> choices = new ArrayList<>(List.of(ingredientItem));
            Utils.convertObjectToList(ingredient.get("alternative")).forEach(alt -> choices.add(
                    toIngredient(Utils.convertObjectToMap(alt))));

            RecipeChoice.ExactChoice choice = new RecipeChoice.ExactChoice(choices);
            if(recipe instanceof ShapelessRecipe)
                ((ShapelessRecipe) recipe).addIngredient(choice);

            if(recipe instanceof ShapedRecipe)
                ((ShapedRecipe) recipe).setIngredient(ingredient.get("letter").toString().charAt(0), choice);
            return;
        }

        if(isNotMaterialDataIngredient(ingredient) && recipe instanceof ShapelessRecipe) {
            ((ShapelessRecipe) recipe).addIngredient(ingredientItem.getType());
            return;
        }

        if(isNotMaterialDataIngredient(ingredient) && recipe instanceof ShapedRecipe) {
            ((ShapedRecipe) recipe).setIngredient(ingredient.get("letter").toString().charAt(0), ingredientItem.getType());
            return;
        }

        MaterialData data = ingredientItem.getData();
        if(data == null)
            return;

        if(recipe instanceof ShapelessRecipe)
            ((ShapelessRecipe) recipe).addIngredient(data);

        if(recipe instanceof ShapedRecipe)
            ((ShapedRecipe) recipe).setIngredient(ingredient.get("letter").toString().charAt(0), data);
    }

    public static @NotNull FurnaceRecipe initFurnaceRecipe(NamespacedKey key, ItemStack result, Object rawIngredient, float exp, int cookingTime) {
        Map<?, ?> ingredient = Utils.convertObjectToMap(Utils.convertObjectToList(rawIngredient).get(0));
        ItemStack ingredientItem = toIngredient(ingredient);

        List<ItemStack> choices = new ArrayList<>(List.of(ingredientItem));
        if(ingredient.containsKey("alternative"))
            Utils.convertObjectToList(ingredient.get("alternative")).forEach(alt -> choices.add(
                    toIngredient(Utils.convertObjectToMap(alt))));

        RecipeChoice.ExactChoice choice = new RecipeChoice.ExactChoice(choices);
        FurnaceRecipe recipe = new FurnaceRecipe(key, result, choice, exp, cookingTime);
        CustomRecipes.registerRecipeLocally(key);
        return recipe;
    }
}
