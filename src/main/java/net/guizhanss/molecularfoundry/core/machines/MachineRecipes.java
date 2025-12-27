package net.guizhanss.molecularfoundry.core.machines;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

import net.guizhanss.molecularfoundry.MolecularFoundry;

public class MachineRecipes {
    public static void register() {
        registerSolarGenerator();
        registerCoalGenerator();
        registerMolecularSynthesizer();
        registerRecombinator();
    }

    private static void registerSolarGenerator() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "solar_generator");
        ShapedRecipe recipe = new ShapedRecipe(key, MachineItems.create(MachineType.SOLAR_GENERATOR));
        recipe.shape("GGG", "DDD", "IRI");
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('D', Material.DAYLIGHT_DETECTOR);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe);
    }

    private static void registerCoalGenerator() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "coal_generator");
        ShapedRecipe recipe = new ShapedRecipe(key, MachineItems.create(MachineType.COAL_GENERATOR));
        recipe.shape("III", "IFI", "CRC");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('F', Material.FURNACE);
        recipe.setIngredient('C', Material.COAL_BLOCK);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        Bukkit.addRecipe(recipe);
    }

    private static void registerMolecularSynthesizer() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "molecular_synthesizer");
        ShapedRecipe recipe = new ShapedRecipe(key, MachineItems.create(MachineType.MOLECULAR_SYNTHESIZER));
        recipe.shape("DDD", "GBG", "IRI");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('G', Material.GLOWSTONE);
        recipe.setIngredient('B', Material.BLAST_FURNACE);
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        Bukkit.addRecipe(recipe);
    }

    private static void registerRecombinator() {
        NamespacedKey key = new NamespacedKey(MolecularFoundry.getInstance(), "recombinator");
        ShapedRecipe recipe = new ShapedRecipe(key, MachineItems.create(MachineType.RECOMBINATOR));
        recipe.shape("EEE", "SBS", "IRI");
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('S', Material.BLAZE_ROD);
        recipe.setIngredient('B', Material.BREWING_STAND);
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        Bukkit.addRecipe(recipe);
    }
}
