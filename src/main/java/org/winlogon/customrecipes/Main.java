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
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Main extends JavaPlugin implements Listener {
    ArrayList<ShapedRecipe> shapedRecipes = new ArrayList<>();
    ArrayList<FurnaceRecipe> smeltingRecipes = new ArrayList<>();
    ArrayList<ShapelessRecipe> shapelessRecipes = new ArrayList<>();
    ArrayList<BlastingRecipe> blastingRecipes = new ArrayList<>();
    Map<NamespacedKey, Consumer<Player>> onEatHandlers = new HashMap<>();

    MiniMessage serializer = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.gradient())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.reset())
                    .build())
            .build();

    @Override
    public void onEnable() {
        createRecipe(null, null)
                .key("removed_saddle")
                .outputMaterial(Material.SADDLE)
                .shape("AAA", "ABA", "B B")
                .ingredient('A', Material.LEATHER)
                .ingredient('B', Material.IRON_INGOT)
                .register();

        createRecipe("Super Toast", new String[]{"#5e28fb", "#af37fa"})
                .outputMaterial(Material.BREAD)
                .shape("AAA", "BCB", "AAA")
                .ingredient('A', Material.BREAD)
                .ingredient('B', Material.WHEAT)
                .ingredient('C', Material.GOLD_BLOCK)
                .onEat(player -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3628, 3));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 511, 5));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 3628, 2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 3628, 2));
                    player.setSaturation(20f);
                    player.setFoodLevel(20);
                })
                .register();

        createRecipe(null, null)
                .key("nametag_item")
                .outputMaterial(Material.NAME_TAG)
                .shape(" AC", " AA", "B  ")
                .ingredient('A', Material.STRING)
                .ingredient('B', Material.PAPER)
                .ingredient('C', Material.EMERALD)
                .register();

        createRecipe("Stone", "#83909B")
                .compressed(true)
                .outputMaterial(Material.STONE)
                .register();

        createRecipe("Dirt", "#A06A5A")
                .compressed(true)
                .outputMaterial(Material.DIRT)
                .register();

        createRecipe("Cobblestone", "#4F5E67")
                .compressed(true)
                .outputMaterial(Material.COBBLESTONE)
                .register();

        createRecipe("Treated Rotten Flesh", "#b25024")
                .type(FurnaceRecipe.class)
                .inputMaterial(Material.ROTTEN_FLESH)
                .outputMaterial(Material.COOKED_RABBIT)
                .experience(0.5f)
                .cookingTime(260)
                .register();

        createRecipe("Zombie Skin", "#59714f")
                .type(BlastingRecipe.class)
                .inputMaterial(Material.ROTTEN_FLESH)
                .outputMaterial(Material.LEATHER)
                .experience(0.75f)
                .cookingTime(420)
                .register();

        shapedRecipes.forEach(recipe -> getServer().addRecipe(recipe));
        smeltingRecipes.forEach(recipe -> getServer().addRecipe(recipe));
        blastingRecipes.forEach(recipe -> getServer().addRecipe(recipe));
        shapelessRecipes.forEach(recipe -> getServer().addRecipe(recipe));

        getServer().getPluginManager().registerEvents(this, this);
    }

    private RecipeBuilder createRecipe(String displayName, Object nameColor) {
        return new RecipeBuilder(
            this, displayName, nameColor, serializer, shapedRecipes, smeltingRecipes, 
            shapelessRecipes, blastingRecipes, onEatHandlers
        );
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

        for (NamespacedKey key : onEatHandlers.keySet()) {
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                onEatHandlers.get(key).accept(eater);
                break;
            }
        }
    }

    public void addOnEatHandler(NamespacedKey key, Consumer<Player> handler) {
        onEatHandlers.put(key, handler);
    }
}
