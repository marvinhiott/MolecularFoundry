package net.guizhanss.molecularfoundry.core.processing;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MachineInventoryHolder {
    private final Block block; private final Location loc; private final String type;
    private final Inventory inv; private final int[] inputSlots; private final int[] outputSlots;

    public MachineInventoryHolder(@Nonnull Block b, @Nonnull String type) {
        this.block = b; this.loc = b.getLocation(); this.type = type;
        this.inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + type);
        switch (type) {
            case "molecular_synthesizer":
                inputSlots = new int[]{20, 24}; outputSlots = new int[]{22, 23, 24}; break;
            case "solar":
                inputSlots = new int[]{}; outputSlots = new int[]{}; break;
            default:
                inputSlots = new int[]{13}; outputSlots = new int[]{31};
        }
        // Minimal GUI; future: add panes and indicators
    }

    @Nonnull public Inventory getInventory() { return inv; }
    @Nullable public int[] getInputSlots() { return inputSlots; }
    @Nullable public int[] getOutputSlots() { return outputSlots; }
}
