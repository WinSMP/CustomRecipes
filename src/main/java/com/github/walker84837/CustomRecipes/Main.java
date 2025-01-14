package com.github.walker84837.CustomRecipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class Main extends JavaPlugin {
    ArrayList<ShapedRecipe> shapedRecipes = new ArrayList<ShapedRecipe>();

    @Override
    public void onEnable() {
        shapedRecipes.add(addSaddleRecipe());
        shapedRecipes.add(addBreadRecipe());

        for (ShapedRecipe recipe : shapedRecipes) {
            getServer().addRecipe(recipe);
        }
    }

    // Adds the old, removed saddle recipe
    private ShapedRecipe addSaddleRecipe() {
        // Create a NamespacedKey for the recipe
        NamespacedKey saddleKey = new NamespacedKey(this, "removed_saddle");

        // Create the result ItemStack (Saddle)
        ItemStack result = new ItemStack(Material.SADDLE);

        // Create the ShapedRecipe for the saddle
        ShapedRecipe saddleRecipe = new ShapedRecipe(saddleKey, result);
        saddleRecipe.shape("AAA", "ABA", "B B");

        // Set the ingredients for the recipe
        saddleRecipe.setIngredient('A', Material.LEATHER);
        saddleRecipe.setIngredient('B', Material.IRON_INGOT);

        return saddleRecipe;
    }

    // Adds a placeholder recipe for bread with NBT data
    // TODO: add custom and colored name (Component API -> MiniMessage)
    private ShapedRecipe addBreadRecipe() {
        // Create a NamespacedKey for the recipe
        NamespacedKey breadKey = new NamespacedKey(this, "super_toast");

        // Create the result ItemStack (Bread)
        ItemStack breadItem = new ItemStack(Material.BREAD);

        // TODO: set NBT identifier and custom name
        // TODO: add effects to apply to user when toast is eaten
        breadItem.getItemMeta().getPersistentDataContainer().set(
            new NamespacedKey(this, "placeholderNBT"), PersistentDataType.STRING, "placeholder"
        );

        ShapedRecipe breadRecipe = new ShapedRecipe(breadKey, breadItem);
        breadRecipe.shape("AAA");

        breadRecipe.setIngredient('A', Material.WHEAT);

        // Register the recipe
        return breadRecipe;
    }
}
