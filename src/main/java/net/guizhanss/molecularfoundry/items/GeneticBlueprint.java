package net.guizhanss.molecularfoundry.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.guizhanss.molecularfoundry.MolecularFoundry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticBlueprint {
    private static final NamespacedKey TARGET_KEY = new NamespacedKey(MolecularFoundry.getInstance(), "mf_blueprint_target");
    private static final NamespacedKey GENES_KEY = new NamespacedKey(MolecularFoundry.getInstance(), "mf_blueprint_genes");
    private static final Random RANDOM = new Random();

    // Each blueprint has 4 gene pairs (8 genes total), each gene is 0-3
    // Genes determine the final material through a lookup table
    
    public static ItemStack create(Material target, int[] genes) {
        if (genes == null || genes.length != 8) {
            genes = generateRandomGenes(target);
        }
        
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName("\u00a7bGenetic Blueprint");
        
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(TARGET_KEY, PersistentDataType.STRING, target.name());
        pdc.set(GENES_KEY, PersistentDataType.STRING, genesToString(genes));
        
        List<String> lore = new ArrayList<>();
        lore.add("\u00a77Target: \u00a7e" + target.name());
        lore.add("\u00a77Genotype:");
        lore.add("\u00a78  " + formatGenePair(genes[0], genes[1]));
        lore.add("\u00a78  " + formatGenePair(genes[2], genes[3]));
        lore.add("\u00a78  " + formatGenePair(genes[4], genes[5]));
        lore.add("\u00a78  " + formatGenePair(genes[6], genes[7]));
        meta.setLore(lore);
        
        paper.setItemMeta(meta);
        return paper;
    }

    public static boolean isGeneticBlueprint(ItemStack stack) {
        if (stack == null || stack.getType() != Material.PAPER) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(GENES_KEY, PersistentDataType.STRING);
    }

    public static Material getTarget(ItemStack stack) {
        if (!isGeneticBlueprint(stack)) return null;
        String name = stack.getItemMeta().getPersistentDataContainer().get(TARGET_KEY, PersistentDataType.STRING);
        try { return Material.valueOf(name); } catch (Exception e) { return null; }
    }

    public static int[] getGenes(ItemStack stack) {
        if (!isGeneticBlueprint(stack)) return null;
        String genesStr = stack.getItemMeta().getPersistentDataContainer().get(GENES_KEY, PersistentDataType.STRING);
        return stringToGenes(genesStr);
    }

    public static ItemStack recombine(ItemStack parent1, ItemStack parent2) {
        if (!isGeneticBlueprint(parent1) || !isGeneticBlueprint(parent2)) return null;
        
        int[] genes1 = getGenes(parent1);
        int[] genes2 = getGenes(parent2);
        if (genes1 == null || genes2 == null) return null;
        
        // Punnett square recombination: randomly select one gene from each pair
        int[] offspring = new int[8];
        for (int i = 0; i < 4; i++) {
            // From parent1, pick first or second gene of pair i
            offspring[i * 2] = RANDOM.nextBoolean() ? genes1[i * 2] : genes1[i * 2 + 1];
            // From parent2, pick first or second gene of pair i
            offspring[i * 2 + 1] = RANDOM.nextBoolean() ? genes2[i * 2] : genes2[i * 2 + 1];
        }
        
        Material resultMaterial = calculateMaterial(offspring);
        return create(resultMaterial, offspring);
    }

    private static Material calculateMaterial(int[] genes) {
        // Sum dominant genes (higher values = more "advanced" materials)
        int strength = 0;
        for (int gene : genes) {
            strength += gene;
        }
        
        // Map strength to materials (0-24 range)
        if (strength >= 22) return Material.NETHERITE_INGOT;
        if (strength >= 20) return Material.DIAMOND;
        if (strength >= 18) return Material.EMERALD;
        if (strength >= 16) return Material.GOLD_INGOT;
        if (strength >= 14) return Material.IRON_INGOT;
        if (strength >= 12) return Material.REDSTONE;
        if (strength >= 10) return Material.LAPIS_LAZULI;
        if (strength >= 8) return Material.COAL;
        if (strength >= 6) return Material.COPPER_INGOT;
        return Material.COBBLESTONE;
    }

    private static int[] generateRandomGenes(Material target) {
        // Generate genes that result in the target material
        int targetStrength = getMaterialStrength(target);
        int[] genes = new int[8];
        
        // Distribute strength across genes
        int remaining = targetStrength;
        for (int i = 0; i < 8; i++) {
            int max = Math.min(3, remaining);
            genes[i] = max > 0 ? RANDOM.nextInt(max + 1) : 0;
            remaining -= genes[i];
        }
        
        // Add any leftover strength randomly
        while (remaining > 0) {
            int pos = RANDOM.nextInt(8);
            if (genes[pos] < 3) {
                genes[pos]++;
                remaining--;
            }
        }
        
        return genes;
    }

    private static int getMaterialStrength(Material material) {
        return switch (material) {
            case NETHERITE_INGOT -> 23;
            case DIAMOND -> 21;
            case EMERALD -> 19;
            case GOLD_INGOT -> 17;
            case IRON_INGOT -> 15;
            case REDSTONE -> 13;
            case LAPIS_LAZULI -> 11;
            case COAL -> 9;
            case COPPER_INGOT -> 7;
            default -> 5;
        };
    }

    private static String formatGenePair(int gene1, int gene2) {
        return toGeneChar(gene1) + "" + toGeneChar(gene2);
    }

    private static char toGeneChar(int gene) {
        return switch (gene) {
            case 0 -> 'a';
            case 1 -> 'b';
            case 2 -> 'c';
            case 3 -> 'd';
            default -> '?';
        };
    }

    private static String genesToString(int[] genes) {
        StringBuilder sb = new StringBuilder();
        for (int gene : genes) {
            sb.append(gene).append(",");
        }
        return sb.toString();
    }

    private static int[] stringToGenes(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        int[] genes = new int[8];
        for (int i = 0; i < Math.min(8, parts.length); i++) {
            try {
                genes[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                genes[i] = 0;
            }
        }
        return genes;
    }
}
