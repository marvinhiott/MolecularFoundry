package net.guizhanss.molecularfoundry.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.guizhanss.molecularfoundry.MolecularFoundry;

public class BlueprintItem {
    private static final NamespacedKey KEY = new NamespacedKey(MolecularFoundry.getInstance(), "mf_blueprint_target");

    public static ItemStack create(Material target) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName("Blueprint: " + target.name());
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY, PersistentDataType.STRING, target.name());
        paper.setItemMeta(meta);
        return paper;
    }

    public static boolean isBlueprint(ItemStack stack) {
        if (stack == null || stack.getType() != Material.PAPER) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(KEY, PersistentDataType.STRING);
    }

    public static Material getTarget(ItemStack stack) {
        if (!isBlueprint(stack)) return null;
        String name = stack.getItemMeta().getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        try { return Material.valueOf(name); } catch (Exception e) { return null; }
    }
}
