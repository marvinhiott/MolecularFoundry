package net.guizhanss.molecularfoundry.core.processing;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class MachineRecipeRegistry {
    private MachineRecipeRegistry() {}

    public static MachineRecipe find(@Nonnull String type, @Nonnull Inventory inv, int[] inSlots) {
        // Minimal: for synthesizer, always process if two items present
        if ("molecular_synthesizer".equals(type)) {
            ItemStack a = inv.getItem(inSlots[0]); ItemStack b = inv.getItem(inSlots[1]);
            if (a != null && b != null) {
                return new MachineRecipe(new ItemStack[]{a, b}, new ItemStack[]{new ItemStack(org.bukkit.Material.DIAMOND)}, 6000, 600);
            }
        }
        return null;
    }
}
