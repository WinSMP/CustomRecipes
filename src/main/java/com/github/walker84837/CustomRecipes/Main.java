package com.github.walker84837.CustomRecipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.*;
import net.kyori.adventure.text.minimessage.tag.standard.*;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.EventHandler;

import io.papermc.paper.persistence.PersistentDataContainerView;

import java.util.ArrayList;

public class Main extends JavaPlugin implements Listener {
    ArrayList<ShapedRecipe> shapedRecipes = new ArrayList<>();
    ArrayList<FurnaceRecipe> smeltingRecipes = new ArrayList<>();
    ArrayList<ShapelessRecipe> shapelessRecipes = new ArrayList<>();

    MiniMessage serializer = MiniMessage.builder()
     .tags(TagResolver.builder()
       .resolver(StandardTags.color())
       .resolver(StandardTags.gradient())
       .resolver(StandardTags.decorations())
       .resolver(StandardTags.reset())
       .build()
     )
     .build();

    @Override
    public void onEnable() {
        shapedRecipes.add(saddleRecipe());
        shapedRecipes.add(breadRecipe());
        shapedRecipes.add(nameTagRecipe());
        shapedRecipes.add(compressedStoneRecipe());
        shapedRecipes.add(compressedDirtRecipe());
        shapedRecipes.add(compressedCobblestoneRecipe());
        shapelessRecipes.add(decompressStoneRecipe());
        shapelessRecipes.add(decompressDirtRecipe());
        shapelessRecipes.add(decompressCobblestoneRecipe());
        smeltingRecipes.add(rottenFleshRecipe());

        for (ShapedRecipe recipe : shapedRecipes) {
            getServer().addRecipe(recipe);
        }

        for (FurnaceRecipe recipe : smeltingRecipes) {
            getServer().addRecipe(recipe);
        }

        for (ShapelessRecipe recipe : shapelessRecipes) {
            getServer().addRecipe(recipe);
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    private ShapedRecipe saddleRecipe() {
        NamespacedKey saddleKey = new NamespacedKey(this, "removed_saddle");
        ItemStack result = new ItemStack(Material.SADDLE);
        ShapedRecipe saddleRecipe = new ShapedRecipe(saddleKey, result);
        saddleRecipe.shape("AAA", "ABA", "B B");
        saddleRecipe.setIngredient('A', Material.LEATHER);
        saddleRecipe.setIngredient('B', Material.IRON_INGOT);
        return saddleRecipe;
    }

    private ShapedRecipe nameTagRecipe() {
        NamespacedKey nameTagKey = new NamespacedKey(this, "nametag_item");
        ItemStack result = new ItemStack(Material.NAME_TAG);
        ShapedRecipe nameTagRecipe = new ShapedRecipe(nameTagKey, result);
        nameTagRecipe.shape(" AC", " AA", "B  ");
        nameTagRecipe.setIngredient('A', Material.STRING);
        nameTagRecipe.setIngredient('B', Material.PAPER);
        nameTagRecipe.setIngredient('C', Material.EMERALD);
        return nameTagRecipe;
    }

    private ShapedRecipe breadRecipe() {
        final String elementIdentifier = "super_toast";

        NamespacedKey breadKey = new NamespacedKey(this, elementIdentifier);
        ItemStack breadItem = new ItemStack(Material.BREAD);
        ItemMeta meta = breadItem.getItemMeta();
        meta.itemName(
            serializer.deserialize("<gradient:#5e28fb:#af37fa>Super Toast</gradient>")
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(this, elementIdentifier), PersistentDataType.STRING, elementIdentifier
        );
        breadItem.setItemMeta(meta);

        ShapedRecipe breadRecipe = new ShapedRecipe(breadKey, breadItem);
        breadRecipe.shape("AAA", "BCB", "AAA");
        breadRecipe.setIngredient('A', Material.BREAD);
        breadRecipe.setIngredient('B', Material.WHEAT);
        breadRecipe.setIngredient('C', Material.GOLD_BLOCK);
        return breadRecipe;
    }

    private FurnaceRecipe rottenFleshRecipe() {
        final String rabbitNbtName = "smelted_rabbit";
        NamespacedKey fleshKey = new NamespacedKey(this, rabbitNbtName);
        ItemStack cookedRabbit = new ItemStack(Material.COOKED_RABBIT);
        ItemMeta meta = cookedRabbit.getItemMeta();

        meta.itemName(serializer.deserialize("<#b25024>Treated Rotten Flesh"));

        meta.getPersistentDataContainer()
            .set(new NamespacedKey(this, rabbitNbtName), PersistentDataType.STRING, rabbitNbtName);

        cookedRabbit.setItemMeta(meta);

        FurnaceRecipe fleshToRabbit = new FurnaceRecipe(
            fleshKey, cookedRabbit, new RecipeChoice.MaterialChoice(Material.ROTTEN_FLESH), 0.5f, 260
        );
        return fleshToRabbit;
    }

    private ShapedRecipe compressedStoneRecipe() {
        final String compressedStoneNbt = "compressed_stone";

        NamespacedKey key = new NamespacedKey(this, compressedStoneNbt);
        ItemStack result = new ItemStack(Material.STONE);
        ItemMeta meta = result.getItemMeta();
        meta.getPersistentDataContainer()
            .set(new NamespacedKey(this, compressedStoneNbt), PersistentDataType.STRING, compressedStoneNbt);
        meta.itemName(
            serializer.deserialize("<#83909B>Compressed Stone")
        );
        result.setItemMeta(meta);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("AAA", "AAA", "AAA");
        recipe.setIngredient('A', Material.STONE);
        return recipe;
    }

    private ShapedRecipe compressedCobblestoneRecipe() {
        final String compressedStoneNbt = "compressed_cobblestone";

        NamespacedKey key = new NamespacedKey(this, compressedStoneNbt);
        ItemStack result = new ItemStack(Material.COBBLESTONE);
        ItemMeta meta = result.getItemMeta();
        meta.getPersistentDataContainer()
            .set(new NamespacedKey(this, compressedStoneNbt), PersistentDataType.STRING, compressedStoneNbt);
        meta.itemName(
            serializer.deserialize("<#4F5E67>Compressed Cobblestone")
        );
        result.setItemMeta(meta);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("AAA", "AAA", "AAA");
        recipe.setIngredient('A', Material.COBBLESTONE);
        return recipe;
    }

    private ShapelessRecipe decompressCobblestoneRecipe() {
        NamespacedKey key = new NamespacedKey(this, "decompress_cobblestone");
        ItemStack result = new ItemStack(Material.COBBLESTONE, 9);
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(new RecipeChoice.ExactChoice(compressedCobblestoneRecipe().getResult()));
        return recipe;
    }
    
    private ShapelessRecipe decompressStoneRecipe() {
        NamespacedKey key = new NamespacedKey(this, "decompress_stone");
        ItemStack result = new ItemStack(Material.STONE, 9);
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(new RecipeChoice.ExactChoice(compressedStoneRecipe().getResult()));
        return recipe;
    }

    private ShapelessRecipe decompressDirtRecipe() {
        NamespacedKey key = new NamespacedKey(this, "decompress_dirt");
        ItemStack result = new ItemStack(Material.DIRT, 9);
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(new RecipeChoice.ExactChoice(compressedDirtRecipe().getResult()));
        return recipe;
    }
    
    private ShapedRecipe compressedDirtRecipe() {
        final String compressedDirtNbt = "compressed_dirt";

        NamespacedKey key = new NamespacedKey(this, "compressed_dirt");
        ItemStack result = new ItemStack(Material.DIRT);
        ItemMeta meta = result.getItemMeta();
        meta.getPersistentDataContainer()
            .set(new NamespacedKey(this, compressedDirtNbt), PersistentDataType.STRING, compressedDirtNbt);
        meta.itemName(
            serializer.deserialize("<#A06A5A>Compressed Dirt")
        );
        result.setItemMeta(meta);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("AAA", "AAA", "AAA");
        recipe.setIngredient('A', Material.DIRT);
        return recipe;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        Player eater = event.getPlayer();

        if (meta == null) {
            Component message = serializer.deserialize(
                "<#F93822>Error: <#D3E0EA>Something went wrong when getting item information!"
            );
            eater.sendMessage(message);
        }

        if (item.getType() == Material.BREAD) {
            PersistentDataContainerView itemPDC = meta.getPersistentDataContainer();
            if (itemPDC.has(new NamespacedKey(this, "super_toast"), PersistentDataType.STRING)) {
                // TODO: set item cooldown
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
