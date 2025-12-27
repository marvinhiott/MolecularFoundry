package net.guizhanss.molecularfoundry.core.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import net.guizhanss.molecularfoundry.MolecularFoundry;
import net.guizhanss.molecularfoundry.core.energy.EnergyManager;
import net.guizhanss.molecularfoundry.core.energy.EnergyStorage;
import net.guizhanss.molecularfoundry.core.energy.EnergyProvider;
import net.guizhanss.molecularfoundry.core.energy.FuelProvider;
import net.guizhanss.molecularfoundry.core.processing.MachineTicker;
import net.guizhanss.molecularfoundry.items.BlueprintItem;
import net.guizhanss.molecularfoundry.util.Keys;

public class BlockListener implements Listener {
    private static final String SYNTH_TITLE = "Molecular Synthesizer";
    private static final String RECOMB_TITLE = "Blueprint Recombinator";
    private static final int SLOT_BLUEPRINT = 10;
    private static final int SLOT_CATALYST = 12;
    private static final int SLOT_PROGRESS = 13;
    private static final int SLOT_ENERGY = 14;
    private static final int SLOT_OUTPUT = 16;
    private static final int REC_SLOT_PARENT1 = 10;
    private static final int REC_SLOT_PARENT2 = 12;
    private static final int REC_SLOT_CATALYST = 14;
    private static final int REC_SLOT_PROGRESS = 13;
    private static final int REC_SLOT_ENERGY = 15;
    private static final int REC_SLOT_OUTPUT = 16;

    @EventHandler public void onPlace(BlockPlaceEvent e) {
        Block b = e.getBlockPlaced(); Material t = b.getType();
        EnergyManager em = MolecularFoundry.getInstance().getEnergyManager(); MachineTicker mt = MolecularFoundry.getInstance().getMachineTicker();
        if (t == Material.DAYLIGHT_DETECTOR) {
            b.getChunk().getPersistentDataContainer().set(Keys.machineKey(b.getLocation()), PersistentDataType.STRING, "solar");
            em.registerStorage(b.getLocation(), new EnergyStorage(b.getLocation(), 500));
            em.registerProvider(b.getLocation(), new EnergyProvider.Solar(b.getLocation(), 5));
        } else if (t == Material.FURNACE) {
            b.getChunk().getPersistentDataContainer().set(Keys.machineKey(b.getLocation()), PersistentDataType.STRING, "coal_generator");
            em.registerStorage(b.getLocation(), new EnergyStorage(b.getLocation(), 1000));
            em.registerProvider(b.getLocation(), new FuelProvider(b.getLocation(), 10));
        } else if (t == Material.BLAST_FURNACE) {
            b.getChunk().getPersistentDataContainer().set(Keys.machineKey(b.getLocation()), PersistentDataType.STRING, "molecular_synthesizer");
            em.registerStorage(b.getLocation(), new EnergyStorage(b.getLocation(), 2000));
            MolecularFoundry.getInstance().getSynthesizerTicker().registerMachine(b);
        } else if (t == Material.BREWING_STAND) {
            b.getChunk().getPersistentDataContainer().set(Keys.machineKey(b.getLocation()), PersistentDataType.STRING, "recombinator");
            em.registerStorage(b.getLocation(), new EnergyStorage(b.getLocation(), 2000));
            MolecularFoundry.getInstance().getRecombinatorTicker().registerMachine(b);
        } else if (t == Material.WHITE_STAINED_GLASS || t == Material.BLUE_STAINED_GLASS || t == Material.YELLOW_STAINED_GLASS || t == Material.TERRACOTTA || t == Material.BLUE_TERRACOTTA || t == Material.CYAN_TERRACOTTA || t == Material.JUKEBOX || t == Material.REDSTONE_LAMP) {
            // Network nodes
            MolecularFoundry.getInstance().getNetworkManager().registerNode(b.getLocation(), t);
        }
    }

    @EventHandler public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        MolecularFoundry.getInstance().getEnergyManager().unregister(b.getLocation());
        MolecularFoundry.getInstance().getSynthesizerTicker().unregisterMachine(b.getLocation());
        MolecularFoundry.getInstance().getRecombinatorTicker().unregisterMachine(b.getLocation());
        MolecularFoundry.getInstance().getNetworkManager().unregisterNode(b.getLocation());
        b.getChunk().getPersistentDataContainer().remove(Keys.machineKey(b.getLocation()));
        b.getChunk().getPersistentDataContainer().remove(Keys.energyKey(b.getLocation()));
    }

    @EventHandler public void onInteract(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return; // ignore off-hand to avoid double-open
        Block b = e.getClickedBlock(); if (b == null) return;
        if (b.getType() == Material.BLAST_FURNACE) {
            MolecularFoundry.getInstance().getSynthesizerTicker().openSynth(b.getLocation(), e.getPlayer());
            e.setCancelled(true);
        } else if (b.getType() == Material.BREWING_STAND) {
            MolecularFoundry.getInstance().getRecombinatorTicker().openRecombinator(b.getLocation(), e.getPlayer());
            e.setCancelled(true);
        } else {
            MolecularFoundry.getInstance().getNetworkManager().handleInteract(b, e.getPlayer());
        }
    }

    @EventHandler public void onInventoryClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (SYNTH_TITLE.equals(title)) {
            handleSynthClick(e);
        } else if (RECOMB_TITLE.equals(title)) {
            handleRecombClick(e);
        } else {
            MolecularFoundry.getInstance().getNetworkManager().handleInventoryClick(e);
        }
    }

    @EventHandler public void onInventoryDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (SYNTH_TITLE.equals(title) || RECOMB_TITLE.equals(title)) {
            int topSize = e.getView().getTopInventory().getSize();
            for (int slot : e.getRawSlots()) {
                if (slot < topSize) { e.setCancelled(true); return; }
            }
        } else {
            MolecularFoundry.getInstance().getNetworkManager().handleInventoryDrag(e);
        }
    }

    private boolean isCursorEmpty(InventoryClickEvent e) {
        return e.getCursor() == null || e.getCursor().getType() == Material.AIR;
    }

    private void handleSynthClick(InventoryClickEvent e) {
        int raw = e.getRawSlot();
        int topSize = e.getView().getTopInventory().getSize();
        if (e.isShiftClick()) { e.setCancelled(true); return; }
        if (raw >= topSize) return;
        switch (raw) {
            case SLOT_BLUEPRINT -> {
                if (!isCursorEmpty(e) && !BlueprintItem.isBlueprint(e.getCursor())) e.setCancelled(true);
            }
            case SLOT_CATALYST -> {
                if (!isCursorEmpty(e) && e.getCursor().getType() != Material.GLOWSTONE_DUST) e.setCancelled(true);
            }
            case SLOT_PROGRESS, SLOT_ENERGY -> e.setCancelled(true);
            case SLOT_OUTPUT -> { if (!isCursorEmpty(e)) e.setCancelled(true); }
            default -> e.setCancelled(true);
        }
    }

    private void handleRecombClick(InventoryClickEvent e) {
        int raw = e.getRawSlot();
        int topSize = e.getView().getTopInventory().getSize();
        if (e.isShiftClick()) { e.setCancelled(true); return; }
        if (raw >= topSize) return;
        switch (raw) {
            case REC_SLOT_PARENT1, REC_SLOT_PARENT2 -> {
                if (!isCursorEmpty(e) && !net.guizhanss.molecularfoundry.items.GeneticBlueprint.isGeneticBlueprint(e.getCursor())) e.setCancelled(true);
            }
            case REC_SLOT_CATALYST -> {
                if (!isCursorEmpty(e) && e.getCursor().getType() != Material.REDSTONE) e.setCancelled(true);
            }
            case REC_SLOT_PROGRESS, REC_SLOT_ENERGY -> e.setCancelled(true);
            case REC_SLOT_OUTPUT -> { if (!isCursorEmpty(e)) e.setCancelled(true); }
            default -> e.setCancelled(true);
        }
    }
}