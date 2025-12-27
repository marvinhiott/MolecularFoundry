package net.guizhanss.molecularfoundry.core.network;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.guizhanss.molecularfoundry.MolecularFoundry;

public final class NetworkUI {
    private NetworkUI() {}

    private static final String TITLE_INSERTER = "Inserter Filter";
    private static final String TITLE_STORAGE = "Network Storage";
    private static final String TITLE_GETTER = "Getter";
    private static final String TITLE_CONTROLLER = "Network Controller";

    private static final int FILTER_SLOT = 13;

    public static void openInserter(Location loc, Player player) {
        Inventory inv = Bukkit.createInventory(new NetworkHolder(loc, NetworkManager.NodeType.INSERTER), 27, TITLE_INSERTER);
        Material filter = MolecularFoundry.getInstance().getNetworkManager().loadInserterFilter(loc);
        inv.setItem(11, addressItem(MolecularFoundry.getInstance().getNetworkManager().getAddress(loc)));
        if (filter != null) {
            inv.setItem(FILTER_SLOT, labeled(new ItemStack(filter), "Filter"));
        }
        player.openInventory(inv);
    }

    public static void openStorage(Location loc, Player player) {
        Inventory inv = Bukkit.createInventory(new NetworkHolder(loc, NetworkManager.NodeType.STORAGE), 9, TITLE_STORAGE);
        var nm = MolecularFoundry.getInstance().getNetworkManager();
        Material mat = nm.loadStorageMaterial(loc);
        long amt = nm.loadStorageAmount(loc);
        int capacity = nm.getStorageCapacity(loc);
        // Slot 0: Add button (deposit from cursor)
        inv.setItem(0, labeled(new ItemStack(Material.LIME_DYE), "ADD"));
        // Slot 1: Withdraw 1
        inv.setItem(1, labeled(new ItemStack(Material.REDSTONE), "Withdraw 1"));
        // Slot 2: Withdraw 64
        inv.setItem(2, labeled(new ItemStack(Material.REDSTONE_BLOCK), "Withdraw 64"));
        // Slot 3: item count display
        inv.setItem(3, countItem(mat, amt, capacity));
        // Slot 4: stored item (read-only)
        if (mat != null) {
            inv.setItem(4, labeled(new ItemStack(mat), "Stored: " + mat.name()));
        } else {
            inv.setItem(4, labeled(new ItemStack(Material.BARRIER), "Empty"));
        }
        // Slot 8: network indicator
        inv.setItem(8, networkStatus(nm.getAddress(loc), capacity));
        player.openInventory(inv);
    }

    public static void openGetter(Location loc, Player player) {
        Inventory inv = Bukkit.createInventory(new NetworkHolder(loc, NetworkManager.NodeType.GETTER), 27, TITLE_GETTER);
        var nm = MolecularFoundry.getInstance().getNetworkManager();
        inv.setItem(11, addressItem(nm.getAddress(loc)));
        player.openInventory(inv);
    }

    public static void openController(Location loc, Player player) { openController(loc, player, 0); }

    public static void openController(Location loc, Player player, int page) {
        var nm = MolecularFoundry.getInstance().getNetworkManager();
        if (!nm.isControllerPowered(loc)) {
            player.sendMessage("\u00a7cController unpowered. Need " + net.guizhanss.molecularfoundry.util.Config.getControllerTickCost() + " RF/tick.");
            return;
        }
        Inventory inv = Bukkit.createInventory(new NetworkHolder(loc, NetworkManager.NodeType.CONTROLLER), 27, TITLE_CONTROLLER);
        java.util.List<NetworkManager.StorageInfo> storages = nm.listStorages();
        int pageSize = 25;
        int start = Math.max(0, page * pageSize);
        int end = Math.min(storages.size(), start + pageSize);
        int slot = 0;
        for (int i = start; i < end; i++) {
            NetworkManager.StorageInfo si = storages.get(i);
            ItemStack item = new ItemStack(si.material == null ? Material.BARREL : si.material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((si.material == null ? "Empty" : si.material.name()) + " @ " + si.address);
            List<String> lore = new ArrayList<>();
            lore.add("Amount: " + si.amount);
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        // Prev / Next / Page label
        inv.setItem(18, labeled(new ItemStack(Material.ARROW), "Prev"));
        inv.setItem(22, labeled(new ItemStack(Material.PAPER), "Page: " + (page + 1)));
        inv.setItem(26, labeled(new ItemStack(Material.ARROW), "Next"));
        player.openInventory(inv);
    }

    public static void handleClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        int raw = e.getRawSlot();
        int top = e.getView().getTopInventory().getSize();
        if (e.isShiftClick()) { e.setCancelled(true); return; }
        if (raw >= top) return;

        if (TITLE_INSERTER.equals(title)) {
            if (raw == FILTER_SLOT) {
                Location loc = extractLocation(e);
                if (loc != null) {
                    ItemStack cursor = e.getCursor();
                    if (cursor != null && cursor.getType() != Material.AIR) {
                        MolecularFoundry.getInstance().getNetworkManager().saveInserterFilter(loc, cursor.getType());
                    } else {
                        MolecularFoundry.getInstance().getNetworkManager().saveInserterFilter(loc, null);
                    }
                }
            } else {
                e.setCancelled(true);
            }
        } else if (TITLE_STORAGE.equals(title)) {
            Location loc = extractLocation(e);
            if (loc == null) { e.setCancelled(true); return; }
            var nm = MolecularFoundry.getInstance().getNetworkManager();
            switch (raw) {
                case 0 -> { nm.depositFromPlayer(loc, (org.bukkit.entity.Player)e.getWhoClicked()); openStorage(loc, (org.bukkit.entity.Player)e.getWhoClicked()); }
                case 1 -> { nm.withdrawToPlayer(loc, (org.bukkit.entity.Player)e.getWhoClicked(), 1); openStorage(loc, (org.bukkit.entity.Player)e.getWhoClicked()); }
                case 2 -> { nm.withdrawToPlayer(loc, (org.bukkit.entity.Player)e.getWhoClicked(), 64); openStorage(loc, (org.bukkit.entity.Player)e.getWhoClicked()); }
                default -> e.setCancelled(true);
            }
        } else if (TITLE_CONTROLLER.equals(title)) {
            Location cloc = extractLocation(e);
            if (cloc == null) { e.setCancelled(true); return; }
            var nm = MolecularFoundry.getInstance().getNetworkManager();
            if (!nm.canPerformNetworkOperation(cloc)) {
                e.getWhoClicked().sendMessage("\u00a7cNot enough power. Need " + net.guizhanss.molecularfoundry.util.Config.getNetworkOperationCost() + " RF.");
                e.setCancelled(true);
                return;
            }
            if (raw == 18) {
                nm.chargeNetworkOperation(cloc);
                openController(cloc, (org.bukkit.entity.Player)e.getWhoClicked(), Math.max(0, getCurrentPage(e) - 1));
            } else if (raw == 26) {
                nm.chargeNetworkOperation(cloc);
                openController(cloc, (org.bukkit.entity.Player)e.getWhoClicked(), getCurrentPage(e) + 1);
            } else {
                ItemStack clicked = e.getCurrentItem();
                if (clicked != null && clicked.getItemMeta() != null) {
                    String name = clicked.getItemMeta().getDisplayName();
                    int at = name.lastIndexOf(" @ ");
                    if (at > 0) {
                        String addr = name.substring(at + 3);
                        Location sloc = nm.resolveAddress(addr);
                        if (sloc != null) {
                            nm.chargeNetworkOperation(cloc);
                            openStorage(sloc, (org.bukkit.entity.Player)e.getWhoClicked());
                        }
                    }
                }
            }
        } else {
            e.setCancelled(true);
        }
    }

    private static Location extractLocation(InventoryClickEvent e) {
        var holder = e.getView().getTopInventory().getHolder();
        if (holder instanceof NetworkHolder nh) {
            return nh.getLocation();
        }
        return null;
    }

    public static void handleDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (!(TITLE_INSERTER.equals(title) || TITLE_STORAGE.equals(title) || TITLE_GETTER.equals(title) || TITLE_CONTROLLER.equals(title))) {
            return;
        }
        int top = e.getView().getTopInventory().getSize();
        for (int slot : e.getRawSlots()) {
            if (slot < top) { e.setCancelled(true); return; }
        }
    }

    private static int getCurrentPage(InventoryClickEvent e) {
        ItemStack paper = e.getView().getTopInventory().getItem(22);
        if (paper != null && paper.getItemMeta() != null) {
            String name = paper.getItemMeta().getDisplayName();
            try {
                int idx = name.indexOf(": ");
                return Math.max(0, Integer.parseInt(name.substring(idx + 2)) - 1);
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private static ItemStack labeled(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack info(Material mat, long amount) {
        ItemStack item = new ItemStack(mat == null ? Material.BARRIER : mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(mat == null ? "Empty" : mat.name());
            List<String> lore = new ArrayList<>();
            lore.add("Amount: " + amount);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack addressItem(String address) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Address");
            List<String> lore = new ArrayList<>();
            lore.add(address == null ? "(unregistered)" : address);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack countItem(Material mat, long amount, int capacity) {
        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Count");
            List<String> lore = new ArrayList<>();
            lore.add("Stored: " + amount);
            lore.add("Cap: " + capacity);
            lore.add("Pct: " + (capacity > 0 ? (amount * 100 / capacity) : 0) + "%");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack networkStatus(String address, int capacity) {
        ItemStack item = new ItemStack(Material.GLOWSTONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Network");
            List<String> lore = new ArrayList<>();
            lore.add("Addr: " + (address == null ? "?" : address));
            lore.add("Cap: " + capacity);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
