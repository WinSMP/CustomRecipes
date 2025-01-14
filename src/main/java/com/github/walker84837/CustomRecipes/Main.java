package com.github.walker84837.CustomRecipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

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
        smeltingRecipes.add(rottenFleshRecipe());

        for (ShapedRecipe recipe : shapedRecipes) {
            getServer().addRecipe(recipe);
        }

        for (FurnaceRecipe recipe : smeltingRecipes) {
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

    private ShapedRecipe breadRecipe() {
        final String elementIdentifier = "super_toast";

        NamespacedKey breadKey = new NamespacedKey(this, elementIdentifier);
        ItemStack breadItem = new ItemStack(Material.BREAD);
        ItemMeta meta = breadItem.getItemMeta();
        meta.itemName(
            serializer.deserialize("<gradient:#5e4fa2:#f79459>Super Toast</gradient>")
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(this, elementIdentifier), PersistentDataType.STRING, elementIdentifier
        );
        breadItem.setItemMeta(meta);

        ShapedRecipe breadRecipe = new ShapedRecipe(breadKey, breadItem);
        breadRecipe.shape("AAA", "BCB", "AAA");
        breadRecipe.setIngredient('A', Material.BREAD);
        breadRecipe.setIngredient('B', Material.WHEAT);
        breadRecipe.setIngredient('C', Material.DIAMOND_BLOCK);
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
                eater.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 3628, 5));
                eater.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 3628, 2));

                eater.setSaturation(20f);
                eater.setFoodLevel(20);
            }
        }
    }
} 
