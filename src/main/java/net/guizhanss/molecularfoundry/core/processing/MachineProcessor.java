package net.guizhanss.molecularfoundry.core.processing;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import net.guizhanss.molecularfoundry.MolecularFoundry;
import net.guizhanss.molecularfoundry.core.energy.EnergyStorage;

public class MachineProcessor {
    private final Block block; private final Location loc; private final String type;
    private int progress = 0; private MachineRecipe current;

    public MachineProcessor(@Nonnull Block b, @Nonnull String type) { this.block = b; this.loc = b.getLocation(); this.type = type; }

    public boolean tick(@Nonnull Inventory inv, int[] inSlots, int[] outSlots) {
        EnergyStorage storage = MolecularFoundry.getInstance().getEnergyManager().getStorage(loc);
        if (storage == null) return false;
        if (current == null) { current = MachineRecipeRegistry.find(type, inv, inSlots); if (current == null) { progress = 0; return false; } }
        int ept = Math.max(1, current.getEnergyCost()/current.getTime());
        if (!storage.hasEnergy(ept)) return false;
        if (!canOutput(inv, outSlots, current.getOutputs())) return false;
        storage.removeEnergy(ept); progress++;
        if (progress >= current.getTime()) { produce(inv, outSlots, current.getOutputs()); progress = 0; current = null; }
        return true;
    }

    private boolean canOutput(@Nonnull Inventory inv, int[] outSlots, @Nonnull ItemStack[] outputs) {
        Map<Integer, ItemStack> test = new HashMap<>();
        for (int s : outSlots) { ItemStack it = inv.getItem(s); if (it != null) test.put(s, it.clone()); }
        for (ItemStack out : outputs) {
            ItemStack rem = out.clone();
            for (int s : outSlots) {
                ItemStack cur = test.get(s);
                if (cur == null || cur.getType() == Material.AIR) { test.put(s, rem.clone()); rem = null; break; }
                else if (!cur.hasItemMeta() && !rem.hasItemMeta() && cur.getType() == rem.getType()) {
                    int space = cur.getMaxStackSize() - cur.getAmount(); if (space >= rem.getAmount()) { cur.setAmount(cur.getAmount()+rem.getAmount()); rem = null; break; }
                    else if (space > 0) { cur.setAmount(cur.getMaxStackSize()); rem.setAmount(rem.getAmount()-space); }
                }
            }
            if (rem != null && rem.getAmount() > 0) return false;
        }
        return true;
    }

    private void produce(@Nonnull Inventory inv, int[] outSlots, @Nonnull ItemStack[] outputs) {
        for (ItemStack out : outputs) {
            if (out == null || out.getType() == Material.AIR) continue;
            ItemStack add = out; // already fresh
            for (int s : outSlots) {
                ItemStack cur = inv.getItem(s);
                if (cur == null || cur.getType() == Material.AIR) { inv.setItem(s, add); break; }
                else if (!cur.hasItemMeta() && !add.hasItemMeta() && cur.getType() == add.getType()) {
                    int space = cur.getMaxStackSize() - cur.getAmount();
                    if (space >= add.getAmount()) { cur.setAmount(cur.getAmount()+add.getAmount()); break; }
                    else if (space > 0) { cur.setAmount(cur.getMaxStackSize()); add.setAmount(add.getAmount()-space); }
                }
            }
        }
    }

    public int getProgress() { return progress; }
    public String getType() { return type; }
}
