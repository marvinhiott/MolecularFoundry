package net.guizhanss.molecularfoundry.core.ui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.guizhanss.molecularfoundry.core.machines.MachineItems;
import net.guizhanss.molecularfoundry.core.machines.MachineType;
import net.guizhanss.molecularfoundry.core.network.NetworkManager;
import net.guizhanss.molecularfoundry.core.network.NetworkItems;

public final class PluginMenu {
    private PluginMenu() {}

    private static final String TITLE_MAIN = "Molecular Foundry";
    private static final String TITLE_MACHINES = "Machines";
    private static final String TITLE_NETWORK = "Network";

    public static boolean isPluginMenu(String title) {
        return TITLE_MAIN.equals(title) || TITLE_MACHINES.equals(title) || TITLE_NETWORK.equals(title);
    }

    public static void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_MAIN);
        inv.setItem(11, labeled(new ItemStack(Material.IRON_BLOCK), "Machines"));
        inv.setItem(15, labeled(new ItemStack(Material.REDSTONE_LAMP), "Network"));
        player.openInventory(inv);
    }

    public static void openMachines(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_MACHINES);
        inv.setItem(10, MachineItems.create(MachineType.SOLAR_GENERATOR));
        inv.setItem(12, MachineItems.create(MachineType.COAL_GENERATOR));
        inv.setItem(14, MachineItems.create(MachineType.MOLECULAR_SYNTHESIZER));
        inv.setItem(16, MachineItems.create(MachineType.RECOMBINATOR));
        // Back
        inv.setItem(22, labeled(new ItemStack(Material.ARROW), "Back"));
        player.openInventory(inv);
    }

    public static void openNetwork(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_NETWORK);
        // Transport tube, Inserter, Getter, Controller, Storage variants
        inv.setItem(10, NetworkItems.createTransportTube());
        inv.setItem(11, NetworkItems.createInserter());
        inv.setItem(12, NetworkItems.createGetter());
        inv.setItem(13, NetworkItems.createController());
        inv.setItem(14, NetworkItems.createJukeboxController());
        inv.setItem(15, NetworkItems.createStorage1k());
        inv.setItem(16, NetworkItems.createStorage5k());
        inv.setItem(17, NetworkItems.createStorage10k());
        // Back
        inv.setItem(22, labeled(new ItemStack(Material.ARROW), "Back"));
        player.openInventory(inv);
    }

    public static void handleClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        int raw = e.getRawSlot();
        int top = e.getView().getTopInventory().getSize();
        if (e.isShiftClick()) { e.setCancelled(true); return; }
        if (raw >= top) return;
        if (TITLE_MAIN.equals(title)) {
            switch (raw) {
                case 11 -> openMachines((Player)e.getWhoClicked());
                case 15 -> openNetwork((Player)e.getWhoClicked());
                default -> e.setCancelled(true);
            }
        } else if (TITLE_MACHINES.equals(title)) {
            switch (raw) {
                case 10 -> give((Player)e.getWhoClicked(), MachineItems.create(MachineType.SOLAR_GENERATOR));
                case 12 -> give((Player)e.getWhoClicked(), MachineItems.create(MachineType.COAL_GENERATOR));
                case 14 -> give((Player)e.getWhoClicked(), MachineItems.create(MachineType.MOLECULAR_SYNTHESIZER));
                case 16 -> give((Player)e.getWhoClicked(), MachineItems.create(MachineType.RECOMBINATOR));
                case 22 -> openMain((Player)e.getWhoClicked());
                default -> e.setCancelled(true);
            }
        } else if (TITLE_NETWORK.equals(title)) {
            switch (raw) {
                case 10 -> give((Player)e.getWhoClicked(), NetworkItems.createTransportTube());
                case 11 -> give((Player)e.getWhoClicked(), NetworkItems.createInserter());
                case 12 -> give((Player)e.getWhoClicked(), NetworkItems.createGetter());
                case 13 -> give((Player)e.getWhoClicked(), NetworkItems.createController());
                case 14 -> give((Player)e.getWhoClicked(), NetworkItems.createJukeboxController());
                case 15 -> give((Player)e.getWhoClicked(), NetworkItems.createStorage1k());
                case 16 -> give((Player)e.getWhoClicked(), NetworkItems.createStorage5k());
                case 17 -> give((Player)e.getWhoClicked(), NetworkItems.createStorage10k());
                case 22 -> openMain((Player)e.getWhoClicked());
                default -> e.setCancelled(true);
            }
        }
    }

    public static void handleDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (!isPluginMenu(title)) return;
        int top = e.getView().getTopInventory().getSize();
        for (int slot : e.getRawSlots()) {
            if (slot < top) { e.setCancelled(true); return; }
        }
    }

    private static ItemStack labeled(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void give(Player player, ItemStack item) {
        player.getInventory().addItem(item);
    }
}
