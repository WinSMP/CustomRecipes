package org.winlogon.customrecipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RecipeBuilder {
    private final CustomRecipes plugin;
    private final String displayName;
    private final Object nameColor;
    private final MiniMessage serializer;
    private final List<ShapedRecipe> shapedRecipes;
    private final List<FurnaceRecipe> smeltingRecipes;
    private final List<ShapelessRecipe> shapelessRecipes;
    private final List<BlastingRecipe> blastingRecipes;
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

    public RecipeBuilder(CustomRecipes plugin, String displayName, Object nameColor, MiniMessage serializer,
                         List<ShapedRecipe> shapedRecipes,
                         List<FurnaceRecipe> smeltingRecipes,
                         List<ShapelessRecipe> shapelessRecipes,
                         List<BlastingRecipe> blastingRecipes,
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
        final String keyName;
        if (customKey != null) {
            keyName = customKey;
        } else if (displayName != null && !displayName.isEmpty()) {
            keyName = (compressed ? "Compressed " + displayName : displayName)
                          .toLowerCase().replace(' ', '_');
        } else {
            throw new IllegalArgumentException("Recipe must have a display name or a custom key");
        }
        
        var recipeKey = new NamespacedKey(plugin, keyName);
        var result = new ItemStack(outputMaterial);
        result.editMeta(meta -> {
            if (compressed) meta.setEnchantmentGlintOverride(true);
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "compressed"), PersistentDataType.BOOLEAN, true);
        });

        var meta = result.getItemMeta();
        Component displayComponent = null;
        NamespacedKey nbtNamespacedKey = null;
    
        if (displayName != null && !displayName.isEmpty()) {
            var finalDisplayName = compressed ? "Compressed " + displayName : displayName;
    
            displayComponent = switch (nameColor) {
                case String color -> serializer.deserialize(STR."<\{color}>\{finalDisplayName}");
                case String[] colors -> serializer.deserialize(STR."<gradient:\{colors[0]}:\{colors[1]}>\{finalDisplayName}</gradient>");
                default -> throw new IllegalArgumentException("Invalid color type");
            };
    
            var nbtKey = keyName + "_nbt";
            nbtNamespacedKey = new NamespacedKey(plugin, nbtKey);
    
            if (meta != null) {
                meta.itemName(displayComponent);
                meta.getPersistentDataContainer().set(nbtNamespacedKey, PersistentDataType.STRING, nbtKey);
            }
        }
    
        if (meta != null) {
            result.setItemMeta(meta);
        }
    
        if (compressed) {
            var shaped = new ShapedRecipe(recipeKey, result);

            var outputStack = new ItemStack(outputMaterial);
            var plain = new RecipeChoice.ExactChoice(outputStack);

            shaped.shape("AAA", "AAA", "AAA");
            shaped.setIngredient('A', plain);
            shapedRecipes.add(shaped);
    
            var decompressKey = new NamespacedKey(plugin, "decompress_" + keyName);
            var decompressResult = new ItemStack(outputMaterial, 9);

            var shapeless = new ShapelessRecipe(decompressKey, decompressResult);
            shapeless.addIngredient(new RecipeChoice.ExactChoice(result));
            shapelessRecipes.add(shapeless);
        } else {
            switch (recipeType.getSimpleName()) {
                case "FurnaceRecipe" -> {
                    var furnaceRecipe = new FurnaceRecipe(
                            recipeKey,
                            result,
                            new RecipeChoice.MaterialChoice(inputMaterial),
                            experience,
                            cookingTime
                    );
                    smeltingRecipes.add(furnaceRecipe);
                }
                case "BlastingRecipe" -> {
                    var blastingRecipe = new BlastingRecipe(
                            recipeKey,
                            result,
                            new RecipeChoice.MaterialChoice(inputMaterial),
                            experience,
                            cookingTime
                    );
                    blastingRecipes.add(blastingRecipe);
                }
                case "ShapedRecipe" -> {
                    var shaped = new ShapedRecipe(recipeKey, result);
                    shaped.shape(shape);
                    ingredients.forEach(shaped::setIngredient);
                    shapedRecipes.add(shaped);
                }
                case "ShapelessRecipe" -> {
                    var shapeless = new ShapelessRecipe(recipeKey, result);
                    ingredients.values().forEach(shapeless::addIngredient);
                    shapelessRecipes.add(shapeless);
                }
                default -> throw new IllegalArgumentException("Unsupported recipe type: " + recipeType.getName());
            }
        }
    
        if (onEat != null && nbtNamespacedKey != null) {
            onEatHandlers.put(nbtNamespacedKey, onEat);
        }
    }
}
