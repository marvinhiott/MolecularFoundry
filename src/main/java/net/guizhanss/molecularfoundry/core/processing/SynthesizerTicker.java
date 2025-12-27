package net.guizhanss.molecularfoundry.core.processing;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

import net.guizhanss.molecularfoundry.MolecularFoundry;
import net.guizhanss.molecularfoundry.core.energy.EnergyManager;
import net.guizhanss.molecularfoundry.core.energy.EnergyStorage;
import net.guizhanss.molecularfoundry.items.BlueprintItem;
import net.guizhanss.molecularfoundry.util.InventoryIO;
import net.guizhanss.molecularfoundry.util.Keys;

public class SynthesizerTicker extends BukkitRunnable {
    private final Map<Location, Inventory> inventories = new HashMap<>();
    private final Map<Location, Integer> progress = new HashMap<>();

    public void registerMachine(Block block) {
        Location loc = block.getLocation();
        Inventory inv = loadInventory(loc);
        inventories.put(loc, inv);
    }

    public void unregisterMachine(Location loc) {
        inventories.remove(loc);
        progress.remove(loc);
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        pdc.remove(Keys.synthInventory(loc));
        pdc.remove(Keys.synthProgress(loc));
    }

    public void openSynth(Location loc, Player player) {
        Inventory inv = inventories.get(loc);
        if (inv == null) {
            inv = loadInventory(loc);
            inventories.put(loc, inv);
        }
        player.openInventory(inv);
    }

    private static final int SLOT_BLUEPRINT = 10;
    private static final int SLOT_CATALYST = 12;
    private static final int SLOT_PROGRESS = 13;
    private static final int SLOT_ENERGY = 14;
    private static final int SLOT_OUTPUT = 16;
    private static final int ENERGY_PER_TICK = 5;
    private static final int TICKS_TO_COMPLETE = 200;

    @Override public void run() {
        EnergyManager em = MolecularFoundry.getInstance().getEnergyManager();
        for (Map.Entry<Location, Inventory> entry : inventories.entrySet()) {
            Location loc = entry.getKey(); Inventory inv = entry.getValue();
            EnergyStorage storage = em.getStorage(loc);
            int currentEnergy = storage != null ? storage.getEnergy() : 0;
            int maxEnergy = storage != null ? storage.getCapacity() : 0;
            
            ItemStack blueprint = inv.getItem(SLOT_BLUEPRINT);
            ItemStack catalyst = inv.getItem(SLOT_CATALYST);
            int prog = progress.getOrDefault(loc, 0);
            
            boolean canProcess = blueprint != null && catalyst != null 
                && BlueprintItem.isBlueprint(blueprint) 
                && catalyst.getType() == Material.GLOWSTONE_DUST
                && storage != null;
            
            if (canProcess) {
                int taken = storage.removeEnergy(ENERGY_PER_TICK);
                if (taken >= ENERGY_PER_TICK) {
                    prog++;
                    if (prog >= TICKS_TO_COMPLETE) {
                        Material target = BlueprintItem.getTarget(blueprint);
                        if (target != null) {
                            produce(inv, target);
                            catalyst.setAmount(catalyst.getAmount() - 1);
                            if (catalyst.getAmount() <= 0) inv.setItem(SLOT_CATALYST, null);
                        }
                        prog = 0;
                    }
                    progress.put(loc, prog);
                } else {
                    prog = 0;
                    progress.put(loc, 0);
                }
            } else {
                prog = 0;
                progress.remove(loc);
            }
            
            updateIndicators(inv, prog, currentEnergy, maxEnergy, canProcess);
            saveState(loc, inv, prog);
        }
    }

    private Inventory loadInventory(Location loc) {
        Inventory inv = Bukkit.createInventory(null, 27, "Molecular Synthesizer");
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        String raw = pdc.get(Keys.synthInventory(loc), PersistentDataType.STRING);
        if (raw != null && !raw.isEmpty()) {
            ItemStack[] items = InventoryIO.itemArrayFromBase64(raw);
            if (items.length > 0) {
                inv.setContents(items);
            }
        }
        Integer prog = pdc.get(Keys.synthProgress(loc), PersistentDataType.INTEGER);
        if (prog != null) {
            progress.put(loc, prog);
        }
        return inv;
    }

    private void saveState(Location loc, Inventory inv, int prog) {
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        String raw = InventoryIO.itemArrayToBase64(inv.getContents());
        pdc.set(Keys.synthInventory(loc), PersistentDataType.STRING, raw);
        pdc.set(Keys.synthProgress(loc), PersistentDataType.INTEGER, prog);
    }

    private void produce(Inventory inv, Material target) {
        ItemStack out = inv.getItem(SLOT_OUTPUT);
        ItemStack unit = new ItemStack(target);
        if (out == null || out.getType() == Material.AIR) { inv.setItem(SLOT_OUTPUT, unit); return; }
        if (out.getType() == target && out.getAmount() < out.getMaxStackSize()) {
            out.setAmount(out.getAmount() + 1); inv.setItem(SLOT_OUTPUT, out);
        }
    }
    
    private void updateIndicators(Inventory inv, int progress, int energy, int maxEnergy, boolean active) {
        // Progress indicator
        ItemStack progressItem = new ItemStack(active && progress > 0 ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta progressMeta = progressItem.getItemMeta();
        if (progressMeta != null) {
            progressMeta.setDisplayName("§eProgress");
            int percent = TICKS_TO_COMPLETE > 0 ? (progress * 100 / TICKS_TO_COMPLETE) : 0;
            progressMeta.setLore(Arrays.asList(
                active ? "§aProcessing: " + percent + "%" : "§7Idle",
                "§7" + progress + " / " + TICKS_TO_COMPLETE + " ticks"
            ));
            progressItem.setItemMeta(progressMeta);
        }
        inv.setItem(SLOT_PROGRESS, progressItem);
        
        // Energy indicator
        ItemStack energyItem = new ItemStack(Material.REDSTONE, Math.max(1, energy / 100));
        ItemMeta energyMeta = energyItem.getItemMeta();
        if (energyMeta != null) {
            energyMeta.setDisplayName("§cEnergy");
            energyMeta.setLore(Arrays.asList(
                "§7" + energy + " / " + maxEnergy + " RF",
                "§7Drain: " + ENERGY_PER_TICK + " RF/tick"
            ));
            energyItem.setItemMeta(energyMeta);
        }
        inv.setItem(SLOT_ENERGY, energyItem);
    }
}
