package net.guizhanss.molecularfoundry.core.network;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import net.guizhanss.molecularfoundry.MolecularFoundry;

public class NetworkRecipes {
    public static void register() {
        registerTransportTube();
        registerInserter();
        registerGetter();
        registerController();
        registerJukeboxController();
        registerStorage1k();
        registerStorage5k();
        registerStorage10k();
    }

    private static void registerTransportTube() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "transport_tube");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.WHITE_STAINED_GLASS, 8));
        recipe.shape("GGG", "R R", "GGG");
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe);
    }

    private static void registerInserter() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "inserter");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.BLUE_STAINED_GLASS, 1));
        recipe.shape(" H ", "GTG", " R ");
        recipe.setIngredient('H', Material.HOPPER);
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('T', Material.WHITE_STAINED_GLASS);
        recipe.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe);
    }

    private static void registerGetter() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "getter");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.YELLOW_STAINED_GLASS, 1));
        recipe.shape(" D ", "GTG", " R ");
        recipe.setIngredient('D', Material.DROPPER);
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('T', Material.WHITE_STAINED_GLASS);
        recipe.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe);
    }

    private static void registerController() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "controller");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.REDSTONE_LAMP, 1));
        recipe.shape("RGR", "GTG", "RIR");
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GLOWSTONE_DUST);
        recipe.setIngredient('T', Material.WHITE_STAINED_GLASS);
        recipe.setIngredient('I', Material.IRON_INGOT);
        Bukkit.addRecipe(recipe);
    }

    private static void registerJukeboxController() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "jukebox_controller");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.JUKEBOX, 1));
        recipe.shape(" N ", "LTL", " R ");
        recipe.setIngredient('N', Material.NOTE_BLOCK);
        recipe.setIngredient('L', Material.REDSTONE_LAMP);
        recipe.setIngredient('T', Material.WHITE_STAINED_GLASS);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        Bukkit.addRecipe(recipe);
    }

    private static void registerStorage1k() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "storage_1k");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.TERRACOTTA, 1));
        recipe.shape("CCC", "CTB", "RRR");
        recipe.setIngredient('C', Material.CLAY_BALL);
        recipe.setIngredient('T', Material.WHITE_STAINED_GLASS);
        recipe.setIngredient('B', Material.CHEST);
        recipe.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe);
    }

    private static void registerStorage5k() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "storage_5k");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.BLUE_TERRACOTTA, 1));
        recipe.shape("LLL", "LSL", "RRR");
        recipe.setIngredient('L', Material.LAPIS_LAZULI);
        recipe.setIngredient('S', Material.TERRACOTTA);
        recipe.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe);
    }

    private static void registerStorage10k() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "storage_10k");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.CYAN_TERRACOTTA, 1));
        recipe.shape("DDD", "DSL", "RRR");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', Material.BLUE_TERRACOTTA);
        recipe.setIngredient('L', Material.LAPIS_BLOCK);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        Bukkit.addRecipe(recipe);
    }
}
