import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;

public class CustomRecipesPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        addSaddleRecipe();
        addBreadRecipe();
    }

    // Adds the old, removed saddle recipe
    private void addSaddleRecipe() {
        // Create a NamespacedKey for the recipe
        NamespacedKey saddleKey = new NamespacedKey(this, "custom_saddle");

        // Create the result ItemStack (Saddle)
        ItemStack saddleItem = new ItemStack(Material.SADDLE);

        // Create the ShapedRecipe for the saddle
        ShapedRecipe saddleRecipe = new ShapedRecipe(saddleKey, saddleItem);
        saddleRecipe.shape("A A", "ABA", "B B");

        // Set the ingredients for the recipe
        saddleRecipe.setIngredient('A', Material.LEATHER);
        saddleRecipe.setIngredient('B', Material.IRON_INGOT);

        // Register the recipe
        getServer().addRecipe(saddleRecipe);
    }

    // Adds a placeholder recipe for bread with NBT data
    private void addBreadRecipe() {
        // Create a NamespacedKey for the recipe
        NamespacedKey breadKey = new NamespacedKey(this, "custom_bread");

        // Create the result ItemStack (Bread)
        ItemStack breadItem = new ItemStack(Material.BREAD);

        // Add placeholder NBT data (fill in later)
        breadItem.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(this, "placeholderNBT"), PersistentDataType.STRING, "placeholder");

        // Create the ShapedRecipe for bread (example: 3 wheat in a row)
        ShapedRecipe breadRecipe = new ShapedRecipe(breadKey, breadItem);
        breadRecipe.shape("AAA");

        // Set the ingredients for the recipe
        breadRecipe.setIngredient('A', Material.WHEAT);

        // Register the recipe
        getServer().addRecipe(breadRecipe);
    }
}
