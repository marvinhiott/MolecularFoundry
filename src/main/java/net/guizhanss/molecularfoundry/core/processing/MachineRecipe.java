package net.guizhanss.molecularfoundry.core.processing;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class MachineRecipe {
    private final ItemStack[] inputs; private final ItemStack[] outputs; private final int energyCost; private final int time;
    public MachineRecipe(@Nonnull ItemStack[] in, @Nonnull ItemStack[] out, int energyCost, int time) {
        this.inputs = in; this.outputs = out; this.energyCost = energyCost; this.time = time;
    }
    public ItemStack[] getInputs() { return inputs; }
    public ItemStack[] getOutputs() { return outputs; }
    public int getEnergyCost() { return energyCost; }
    public int getTime() { return time; }
    public boolean matches(@Nonnull Inventory inv, int[] inSlots) { return true; }
}
