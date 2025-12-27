package net.guizhanss.molecularfoundry.core.processing;

import net.guizhanss.molecularfoundry.MolecularFoundry;
import net.guizhanss.molecularfoundry.core.energy.EnergyStorage;
import net.guizhanss.molecularfoundry.items.GeneticBlueprint;
import net.guizhanss.molecularfoundry.util.InventoryIO;
import net.guizhanss.molecularfoundry.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class RecombinatorTicker extends BukkitRunnable {
    private final Map<Location, Inventory> inventories = new HashMap<>();
    private final Map<Location, Integer> progress = new HashMap<>();
    
    private static final int SLOT_PARENT1 = 10;
    private static final int SLOT_PARENT2 = 12;
    private static final int SLOT_CATALYST = 14;
    private static final int SLOT_PROGRESS = 13;
    private static final int SLOT_ENERGY = 15;
    private static final int SLOT_OUTPUT = 16;
    
    private static final int ENERGY_PER_TICK = 8;
    private static final int TICKS_TO_COMPLETE = 300; // 15 seconds
    private static final String TITLE = "Blueprint Recombinator";
    
    public void registerMachine(@Nonnull Block block) {
        Location loc = block.getLocation();
        Inventory inv = loadInventory(loc);
        inventories.put(loc, inv);
    }
    
    public void unregisterMachine(@Nonnull Location loc) {
        inventories.remove(loc);
        progress.remove(loc);
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        pdc.remove(Keys.recombInventory(loc));
        pdc.remove(Keys.recombProgress(loc));
    }
    
    public void openRecombinator(@Nonnull Location loc, @Nonnull Player player) {
        Inventory inv = inventories.get(loc);
        if (inv == null) {
            inv = loadInventory(loc);
            inventories.put(loc, inv);
        }
        player.openInventory(inv);
    }
    
    @Override
    public void run() {
        for (Map.Entry<Location, Inventory> entry : inventories.entrySet()) {
            Location loc = entry.getKey();
            Inventory inv = entry.getValue();
            
            EnergyStorage storage = MolecularFoundry.getInstance().getEnergyManager().getStorage(loc);
            if (storage == null) continue;
            
            int currentEnergy = storage.getEnergy();
            int maxEnergy = storage.getCapacity();
            
            ItemStack parent1 = inv.getItem(SLOT_PARENT1);
            ItemStack parent2 = inv.getItem(SLOT_PARENT2);
            ItemStack catalyst = inv.getItem(SLOT_CATALYST);
            
            boolean canProcess = GeneticBlueprint.isGeneticBlueprint(parent1) &&
                                GeneticBlueprint.isGeneticBlueprint(parent2) &&
                                catalyst != null && catalyst.getType() == Material.REDSTONE &&
                                currentEnergy >= ENERGY_PER_TICK;
            
            int currentProgress = progress.getOrDefault(loc, 0);
            
            if (canProcess) {
                storage.removeEnergy(ENERGY_PER_TICK);
                currentProgress++;
                progress.put(loc, currentProgress);
                
                if (currentProgress >= TICKS_TO_COMPLETE) {
                    // Recombine blueprints
                    ItemStack offspring = GeneticBlueprint.recombine(parent1, parent2);
                    if (offspring != null) {
                        produce(inv, offspring);
                        
                        // Consume one of each parent and catalyst
                        parent1.setAmount(parent1.getAmount() - 1);
                        parent2.setAmount(parent2.getAmount() - 1);
                        catalyst.setAmount(catalyst.getAmount() - 1);
                        
                        progress.put(loc, 0);
                    }
                }
            } else {
                if (currentProgress > 0) {
                    currentProgress = Math.max(0, currentProgress - 2);
                    progress.put(loc, currentProgress);
                }
            }
            
            updateIndicators(inv, currentProgress, currentEnergy, maxEnergy, canProcess);
            saveState(loc, inv, currentProgress);
        }
    }

    private Inventory loadInventory(Location loc) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        String raw = pdc.get(Keys.recombInventory(loc), PersistentDataType.STRING);
        if (raw != null && !raw.isEmpty()) {
            ItemStack[] items = InventoryIO.itemArrayFromBase64(raw);
            if (items.length > 0) inv.setContents(items);
        }
        Integer prog = pdc.get(Keys.recombProgress(loc), PersistentDataType.INTEGER);
        if (prog != null) progress.put(loc, prog);
        return inv;
    }

    private void saveState(Location loc, Inventory inv, int prog) {
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        pdc.set(Keys.recombInventory(loc), PersistentDataType.STRING, InventoryIO.itemArrayToBase64(inv.getContents()));
        pdc.set(Keys.recombProgress(loc), PersistentDataType.INTEGER, prog);
    }
    
    private void produce(@Nonnull Inventory inv, @Nonnull ItemStack result) {
        ItemStack existing = inv.getItem(SLOT_OUTPUT);
        if (existing == null || existing.getType() == Material.AIR) {
            inv.setItem(SLOT_OUTPUT, result);
        } else if (existing.isSimilar(result) && existing.getAmount() < existing.getMaxStackSize()) {
            existing.setAmount(existing.getAmount() + 1);
        }
    }
    
    private void updateIndicators(@Nonnull Inventory inv, int progress, int energy, int maxEnergy, boolean active) {
        // Progress indicator
        ItemStack progressItem = new ItemStack(active ? Material.LIME_DYE : Material.GRAY_DYE);
        var progressMeta = progressItem.getItemMeta();
        progressMeta.setDisplayName(active ? "\u00a7aRecombining..." : "\u00a77Idle");
        java.util.List<String> progressLore = new java.util.ArrayList<>();
        int percent = (progress * 100) / TICKS_TO_COMPLETE;
        progressLore.add("\u00a77Progress: \u00a7e" + percent + "%");
        progressLore.add("\u00a78(" + progress + "/" + TICKS_TO_COMPLETE + " ticks)");
        progressMeta.setLore(progressLore);
        progressItem.setItemMeta(progressMeta);
        inv.setItem(SLOT_PROGRESS, progressItem);
        
        // Energy indicator
        ItemStack energyItem = new ItemStack(Material.REDSTONE);
        int displayAmount = Math.max(1, (energy * 64) / maxEnergy);
        energyItem.setAmount(displayAmount);
        var energyMeta = energyItem.getItemMeta();
        energyMeta.setDisplayName("\u00a7cEnergy");
        java.util.List<String> energyLore = new java.util.ArrayList<>();
        energyLore.add("\u00a77Stored: \u00a7c" + energy + " RF");
        energyLore.add("\u00a77Capacity: \u00a7c" + maxEnergy + " RF");
        if (active) energyLore.add("\u00a77Drain: \u00a7c-" + ENERGY_PER_TICK + " RF/t");
        energyMeta.setLore(energyLore);
        energyItem.setItemMeta(energyMeta);
        inv.setItem(SLOT_ENERGY, energyItem);
    }
    
    public void start() {
        this.runTaskTimer(MolecularFoundry.getInstance(), 0L, 1L);
    }
    
    public void stop() {
        this.cancel();
    }
}
