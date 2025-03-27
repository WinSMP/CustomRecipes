package org.winlogon.customrecipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.*;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.*;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.papermc.paper.persistence.PersistentDataContainerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RecipeBuilder {
        private final Main plugin; private final String displayName;
        private final Object nameColor;
        private boolean compressed = false;
        private Class<? extends Recipe> recipeType = ShapedRecipe.class;
        private Material outputMaterial;
        private Material inputMaterial;
        private float experience = 0.0f;
        private int cookingTime = 0;
        private String[] shape;
        private Map<Character, Material> ingredients = new HashMap<>();
        private Consumer<Player> onEat;
        private MiniMessage serializer;

        public RecipeBuilder(Main plugin, String displayName, Object nameColor, MiniMessage serializer) {
            this.plugin = plugin;
            this.displayName = displayName;
            this.nameColor = nameColor;
            this.serializer = serializer;
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

        public void register() {
            String finalDisplayName = compressed ? "Compressed " + displayName : displayName;
            Component displayComponent;

            if (nameColor instanceof String) {
                String color = (String) nameColor;
                displayComponent = serializer.deserialize(String.format("<%s>%s", color, finalDisplayName));
            } else if (nameColor instanceof String[]) {
                String[] colors = (String[]) nameColor;
                displayComponent = serializer.deserialize(String.format("<gradient:%s:%s>%s</gradient>", colors[0], colors[1], finalDisplayName));
            } else {
                throw new IllegalArgumentException("Invalid color type");
            }

            String keyName = finalDisplayName.toLowerCase().replace(' ', '_');
            NamespacedKey recipeKey = new NamespacedKey(plugin, keyName);
            ItemStack result = new ItemStack(outputMaterial);
            ItemMeta meta = result.getItemMeta();
            meta.displayName(displayComponent);
            String nbtKey = keyName + "_nbt";
            NamespacedKey nbtNamespacedKey = new NamespacedKey(plugin, nbtKey);
            meta.getPersistentDataContainer().set(nbtNamespacedKey, PersistentDataType.STRING, nbtKey);
            result.setItemMeta(meta);

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
                FurnaceRecipe furnaceRecipe = new FurnaceRecipe(recipeKey, result, inputMaterial, experience, cookingTime);
                smeltingRecipes.add(furnaceRecipe);
            } else if (BlastingRecipe.class.isAssignableFrom(recipeType)) {
                BlastingRecipe blastingRecipe = new BlastingRecipe(recipeKey, result, inputMaterial, experience, cookingTime);
                blastingRecipes.add(blastingRecipe);
            } else if (recipeType == ShapedRecipe.class) {
                ShapedRecipe shaped = new ShapedRecipe(recipeKey, result);
                shaped.shape(shape);
                ingredients.forEach(shaped::setIngredient);
                shapedRecipes.add(shaped);
            } else if (recipeType == ShapelessRecipe.class) {
                ShapelessRecipe shapeless = new ShapelessRecipe(recipeKey, result);
                ingredients.values().forEach(mat -> shapeless.addIngredient(mat));
                shapelessRecipes.add(shapeless);
            }

            if (onEat != null) {
                onEatHandlers.put(nbtNamespacedKey, onEat);
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        Player eater = event.getPlayer();

        if (meta == null) {
            Component message = serializer.deserialize("<#F93822>Error: <#D3E0EA>Something went wrong when getting item information!");
            eater.sendMessage(message);
            return;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        for (NamespacedKey key : onEatHandlers.keySet()) {
            if (pdc.has(key, PersistentDataType.STRING)) {
                onEatHandlers.get(key).accept(eater);
                break;
            }
        }

        // Existing bread check
        if (item.getType() == Material.BREAD) {
            PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
            if (itemPDC.has(new NamespacedKey(this, "super_toast"), PersistentDataType.STRING)) {
                eater.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3628, 3));
                eater.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 511, 5));
                eater.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 3628, 2));
                eater.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 3628, 2));
                eater.setSaturation(20f);
                eater.setFoodLevel(20);
            }
        }
    }
}
