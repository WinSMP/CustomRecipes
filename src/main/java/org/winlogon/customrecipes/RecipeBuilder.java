package org.winlogon.customrecipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RecipeBuilder {
    private final Main plugin;
    private final String displayName;
    private final Object nameColor;
    private final MiniMessage serializer;
    private final ArrayList<ShapedRecipe> shapedRecipes;
    private final ArrayList<FurnaceRecipe> smeltingRecipes;
    private final ArrayList<ShapelessRecipe> shapelessRecipes;
    private final ArrayList<BlastingRecipe> blastingRecipes;
    private final Map<NamespacedKey, Consumer<Player>> onEatHandlers;

    private boolean compressed = false;
    private Class<? extends Recipe> recipeType = ShapedRecipe.class;
    private Material outputMaterial;
    private Material inputMaterial;
    private float experience = 0.0f;
    private int cookingTime = 0;
    private String[] shape;
    private String customKey = null;
    private Map<Character, Material> ingredients = new HashMap<>();
    private Consumer<Player> onEat;

    public RecipeBuilder(Main plugin, String displayName, Object nameColor, MiniMessage serializer,
                         ArrayList<ShapedRecipe> shapedRecipes,
                         ArrayList<FurnaceRecipe> smeltingRecipes,
                         ArrayList<ShapelessRecipe> shapelessRecipes,
                         ArrayList<BlastingRecipe> blastingRecipes,
                         Map<NamespacedKey, Consumer<Player>> onEatHandlers) {
        this.plugin = plugin;
        this.displayName = displayName;
        this.nameColor = nameColor;
        this.serializer = serializer;
        this.shapedRecipes = shapedRecipes;
        this.smeltingRecipes = smeltingRecipes;
        this.shapelessRecipes = shapelessRecipes;
        this.blastingRecipes = blastingRecipes;
        this.onEatHandlers = onEatHandlers;
    }

    public RecipeBuilder outputMaterial(Material material) {
        this.outputMaterial = material;
        return this;
    }

    public RecipeBuilder inputMaterial(Material material) {
        this.inputMaterial = material;
        return this;
    }

    public RecipeBuilder compressed(boolean compressed) {
        this.compressed = compressed;
        return this;
    }

    public RecipeBuilder type(Class<? extends Recipe> type) {
        this.recipeType = type;
        return this;
    }

    public RecipeBuilder shape(String... shape) {
        this.shape = shape;
        return this;
    }

    public RecipeBuilder ingredient(char key, Material material) {
        this.ingredients.put(key, material);
        return this;
    }

    public RecipeBuilder experience(float experience) {
        this.experience = experience;
        return this;
    }

    public RecipeBuilder cookingTime(int time) {
        this.cookingTime = time;
        return this;
    }

    public RecipeBuilder onEat(Consumer<Player> handler) {
        this.onEat = handler;
        return this;
    }

    public RecipeBuilder key(String key) {
        this.customKey = key;
        return this;
    }

    public void register() {
        String keyName;
        if (customKey != null) {
            keyName = customKey;
        } else if (displayName != null && !displayName.isEmpty()) {
            keyName = (compressed ? "Compressed " + displayName : displayName).toLowerCase().replace(' ', '_');
        } else {
            throw new IllegalArgumentException("Recipe must have a display name or a custom key");
        }

        NamespacedKey recipeKey = new NamespacedKey(plugin, keyName);
        ItemStack result = new ItemStack(outputMaterial);
        ItemMeta meta = result.getItemMeta();

        Component displayComponent = null;
        NamespacedKey nbtNamespacedKey = null;

        if (displayName != null && !displayName.isEmpty()) {
            String finalDisplayName = compressed ? "Compressed " + displayName : displayName;

            if (nameColor instanceof String) {
                String color = (String) nameColor;
                displayComponent = serializer.deserialize(String.format("<%s>%s", color, finalDisplayName));
            } else if (nameColor instanceof String[]) {
                String[] colors = (String[]) nameColor;
                displayComponent = serializer.deserialize(String.format("<gradient:%s:%s>%s</gradient>", colors[0], colors[1], finalDisplayName));
            } else {
                throw new IllegalArgumentException("Invalid color type");
            }

            String nbtKey = keyName + "_nbt";
            nbtNamespacedKey = new NamespacedKey(plugin, nbtKey);

            if (meta != null) {
                meta.displayName(displayComponent);
                meta.getPersistentDataContainer().set(nbtNamespacedKey, PersistentDataType.STRING, nbtKey);
            }
        }

        if (meta != null) {
            result.setItemMeta(meta);
        }

        if (compressed) {
            ShapedRecipe shaped = new ShapedRecipe(recipeKey, result);
            shaped.shape("AAA", "AAA", "AAA");
            shaped.setIngredient('A', outputMaterial);
            shapedRecipes.add(shaped);

            NamespacedKey decompressKey = new NamespacedKey(plugin, "decompress_" + keyName);
            ItemStack decompressResult = new ItemStack(outputMaterial, 9);
            ShapelessRecipe shapeless = new ShapelessRecipe(decompressKey, decompressResult);
            shapeless.addIngredient(new RecipeChoice.ExactChoice(result));
            shapelessRecipes.add(shapeless);
        } else if (FurnaceRecipe.class.isAssignableFrom(recipeType)) {
            FurnaceRecipe furnaceRecipe = new FurnaceRecipe(recipeKey, result, new RecipeChoice.MaterialChoice(inputMaterial), experience, cookingTime);
            smeltingRecipes.add(furnaceRecipe);
        } else if (BlastingRecipe.class.isAssignableFrom(recipeType)) {
            BlastingRecipe blastingRecipe = new BlastingRecipe(recipeKey, result, new RecipeChoice.MaterialChoice(inputMaterial), experience, cookingTime);
            blastingRecipes.add(blastingRecipe);
        } else if (recipeType == ShapedRecipe.class) {
            ShapedRecipe shaped = new ShapedRecipe(recipeKey, result);
            shaped.shape(shape);
            ingredients.forEach((key, material) -> shaped.setIngredient(key, material));
            shapedRecipes.add(shaped);
        } else if (recipeType == ShapelessRecipe.class) {
            ShapelessRecipe shapeless = new ShapelessRecipe(recipeKey, result);
            ingredients.values().forEach(shapeless::addIngredient);
            shapelessRecipes.add(shapeless);
        }

        if (onEat != null && nbtNamespacedKey != null) {
            onEatHandlers.put(nbtNamespacedKey, onEat);
        }
    }
}
