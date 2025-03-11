package org.winlogon.customrecipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

public class Main extends JavaPlugin implements Listener {

    MiniMessage serializer = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.gradient())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.reset())
                    .build())
            .build();

    private final Map<String, ItemStack> customItems = new HashMap<>();
    private final List<Recipe> recipeAnnotations = new ArrayList<>();

    @Override
    public void onEnable() {
        processRecipes();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void processRecipes() {
        Recipe[] recipes = getClass().getAnnotationsByType(Recipe.class);
        // First pass: create all result items
        for (Recipe recipe : recipes) {
            ItemStack result = createResultItem(recipe);
            customItems.put(recipe.id(), result);
            recipeAnnotations.add(recipe);
        }
        // Second pass: create and register recipes
        for (Recipe recipe : recipeAnnotations) {
            NamespacedKey key = new NamespacedKey(this, recipe.id());
            ItemStack result = customItems.get(recipe.id());
            org.bukkit.inventory.Recipe bukkitRecipe = createRecipe(key, result, recipe);
            if (bukkitRecipe != null) {
                getServer().addRecipe(bukkitRecipe);
            } else {
                getLogger().warning("Failed to create recipe: " + recipe.id());
            }
        }
    }

    private ItemStack createResultItem(Recipe annotation) {
        ItemStack item = new ItemStack(annotation.material(), annotation.amount());
        if (!annotation.name().isEmpty() && !annotation.color().isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            String color = annotation.color();
            String name = annotation.name();
            String miniMessage;
            if (color.contains(",")) {
                String[] colors = color.split(",");
                miniMessage = "<gradient:" + String.join(":", colors) + ">" + name + "</gradient>";
            } else {
                miniMessage = "<color:" + color + ">" + name + "</color>";
            }
            meta.displayName(serializer.deserialize(miniMessage));
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(this, annotation.id()),
                    PersistentDataType.STRING,
                    annotation.id()
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private org.bukkit.inventory.Recipe createRecipe(NamespacedKey key, ItemStack result, Recipe annotation) {
        switch (annotation.type()) {
            case SHAPED:
                return createShapedRecipe(key, result, annotation);
            case SHAPELESS:
                return createShapelessRecipe(key, result, annotation);
            case FURNACE:
                return createFurnaceRecipe(key, result, annotation);
            case BLASTING:
                return createBlastingRecipe(key, result, annotation);
            default:
                return null;
        }
    }

    private ShapedRecipe createShapedRecipe(NamespacedKey key, ItemStack result, Recipe recipe) {
        ShapedRecipe shaped = new ShapedRecipe(key, result);
        shaped.shape(recipe.shape());
        for (String keyDef : recipe.keys()) {
            String[] parts = keyDef.split("=");
            if (parts.length != 2) continue;
            char symbol = parts[0].charAt(0);
            Material material = Material.valueOf(parts[1].trim().toUpperCase());
            shaped.setIngredient(symbol, material);
        }
        return shaped;
    }

    private ShapelessRecipe createShapelessRecipe(NamespacedKey key, ItemStack result, Recipe recipe) {
        ShapelessRecipe shapeless = new ShapelessRecipe(key, result);
        for (String ingredient : recipe.ingredients()) {
            if (ingredient.startsWith("custom:")) {
                String id = ingredient.substring(7);
                ItemStack customItem = customItems.get(id);
                if (customItem != null) {
                    shapeless.addIngredient(new RecipeChoice.ExactChoice(customItem));
                }
            } else {
                Material material = Material.valueOf(ingredient.toUpperCase());
                shapeless.addIngredient(material);
            }
        }
        return shapeless;
    }

    private FurnaceRecipe createFurnaceRecipe(NamespacedKey key, ItemStack result, Recipe recipe) {
        return new FurnaceRecipe(
                key,
                result,
                recipe.input(),
                recipe.experience(),
                recipe.cookingTime()
        );
    }

    private BlastingRecipe createBlastingRecipe(NamespacedKey key, ItemStack result, Recipe recipe) {
        return new BlastingRecipe(
                key,
                result,
                recipe.input(),
                recipe.experience(),
                recipe.cookingTime()
        );
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        Player player = event.getPlayer();

        if (meta == null) return;

        String id = meta.getPersistentDataContainer().get(new NamespacedKey(this, "super_toast"), PersistentDataType.STRING);
        if ("super_toast".equals(id)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3628, 3));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 511, 5));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 3628, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 3628, 2));
            player.setSaturation(20f);
            player.setFoodLevel(20);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Recipes.class)
    public @interface Recipe {
        RecipeType type();
        String id();
        Material material();
        String name() default "";
        String color() default "";
        int amount() default 1;
        String[] shape() default {};
        String[] keys() default {};
        String[] ingredients() default {};
        Material input() default Material.AIR;
        float experience() default 0;
        int cookingTime() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Recipes {
        Recipe[] value();
    }

    public enum RecipeType {
        SHAPED, SHAPELESS, FURNACE, BLASTING
    }

    // Example annotations (place these on the Main class)
    // @Recipe(
    //     type = RecipeType.SHAPED,
    //     id = "super_toast",
    //     material = Material.BREAD,
    //     name = "Super Toast",
    //     color = "#5e28fb,#af37fa",
    //     shape = {"AAA", "BCB", "AAA"},
    //     keys = {"A=BREAD", "B=WHEAT", "C=GOLD_BLOCK"}
    // )
    // @Recipe(
    //     type = RecipeType.FURNACE,
    //     id = "treated_flesh",
    //     material = Material.COOKED_RABBIT,
    //     name = "Treated Rotten Flesh",
    //     color = "#b25024",
    //     input = Material.ROTTEN_FLESH,
    //     experience = 0.5f,
    //     cookingTime = 260
    // )
    // Add other @Recipe annotations here
    public static class RecipeAnnotations {}
}
