package net.guizhanss.molecularfoundry.core.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.guizhanss.molecularfoundry.MolecularFoundry;
import net.guizhanss.molecularfoundry.util.Keys;

public class NetworkManager {
    public enum NodeType { TRANSPORT_TUBE, INSERTER, STORAGE, GETTER, CONTROLLER, JUKEBOX_MENU }

    private static final Map<Material, Integer> CAPACITY_BY_BLOCK = new HashMap<>();
    static {
        CAPACITY_BY_BLOCK.put(Material.TERRACOTTA, 1000);
        CAPACITY_BY_BLOCK.put(Material.BLUE_TERRACOTTA, 5000);
        CAPACITY_BY_BLOCK.put(Material.CYAN_TERRACOTTA, 10000);
    }

    private static final Map<Material, NodeType> TYPE_BY_BLOCK = new HashMap<>();
    static {
        TYPE_BY_BLOCK.put(Material.WHITE_STAINED_GLASS, NodeType.TRANSPORT_TUBE);
        TYPE_BY_BLOCK.put(Material.BLUE_STAINED_GLASS, NodeType.INSERTER);
        TYPE_BY_BLOCK.put(Material.TERRACOTTA, NodeType.STORAGE);
        TYPE_BY_BLOCK.put(Material.BLUE_TERRACOTTA, NodeType.STORAGE);
        TYPE_BY_BLOCK.put(Material.CYAN_TERRACOTTA, NodeType.STORAGE);
        TYPE_BY_BLOCK.put(Material.YELLOW_STAINED_GLASS, NodeType.GETTER);
        TYPE_BY_BLOCK.put(Material.REDSTONE_LAMP, NodeType.CONTROLLER);
        TYPE_BY_BLOCK.put(Material.JUKEBOX, NodeType.JUKEBOX_MENU);
    }

    private final Map<Location, NodeType> nodes = new HashMap<>();
    private final Map<Location, String> addresses = new HashMap<>();
    private final Map<String, Location> addressToLoc = new HashMap<>();

    public void registerNode(@Nonnull Location loc, @Nonnull Material blockType) {
        NodeType type = TYPE_BY_BLOCK.get(blockType);
        if (type == null) return;
        nodes.put(loc, type);
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        pdc.set(Keys.networkNode(loc), PersistentDataType.STRING, type.name());
        // Assign simple coordinate-based address for determinism
        String addr = computeAddress(loc);
        addresses.put(loc, addr);
        addressToLoc.put(addr, loc);
        pdc.set(Keys.networkAddress(loc), PersistentDataType.STRING, addr);
        // Save capacity if it's a storage
        if (type == NodeType.STORAGE) {
            int cap = CAPACITY_BY_BLOCK.getOrDefault(blockType, 1000);
            pdc.set(Keys.networkStorageCapacity(loc), PersistentDataType.INTEGER, cap);
        }
        if (type == NodeType.STORAGE && !pdc.has(Keys.networkStorageCapacity(loc), PersistentDataType.INTEGER)) {
            pdc.set(Keys.networkStorageCapacity(loc), PersistentDataType.INTEGER, 1000);
        }
    }

    public void unregisterNode(@Nonnull Location loc) {
        nodes.remove(loc);
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        pdc.remove(Keys.networkNode(loc));
        String addr = addresses.remove(loc);
        if (addr != null) addressToLoc.remove(addr);
        pdc.remove(Keys.networkAddress(loc));
        pdc.remove(Keys.networkStorageMaterial(loc));
        pdc.remove(Keys.networkStorageAmount(loc));
        pdc.remove(Keys.networkInserterFilter(loc));
        pdc.remove(Keys.networkStorageCapacity(loc));
    }

    public void handleInteract(@Nonnull Block block, @Nonnull Player player) {
        NodeType type = nodes.get(block.getLocation());
        if (type == null) return;
        switch (type) {
            case INSERTER -> NetworkUI.openInserter(block.getLocation(), player);
            case STORAGE -> NetworkUI.openStorage(block.getLocation(), player);
            case GETTER -> NetworkUI.openGetter(block.getLocation(), player);
            case CONTROLLER, JUKEBOX_MENU -> NetworkUI.openController(block.getLocation(), player);
            default -> { /* tubes have no UI */ }
        }
    }

    public void handleInventoryClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        NetworkUI.handleClick(e);
    }

    public void handleInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent e) {
        NetworkUI.handleDrag(e);
    }

    public void bootstrapFromLoadedChunks() {
        for (org.bukkit.World world : MolecularFoundry.getInstance().getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                var pdc = chunk.getPersistentDataContainer();
                for (NamespacedKey key : pdc.getKeys()) {
                    String name = key.getKey();
                    if (name.startsWith("mf_net_node_")) {
                        String typeStr = pdc.get(key, PersistentDataType.STRING);
                        if (typeStr == null) continue;
                        try {
                            String[] parts = name.substring("mf_net_node_".length()).split("_");
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);
                            int z = Integer.parseInt(parts[2]);
                            Location loc = new Location(chunk.getWorld(), x, y, z);
                            NodeType type = NodeType.valueOf(typeStr);
                            nodes.put(loc, type);
                            // Load address if present, else compute
                            String addr = pdc.get(Keys.networkAddress(loc), PersistentDataType.STRING);
                            if (addr == null || addr.isEmpty()) addr = computeAddress(loc);
                            addresses.put(loc, addr);
                            addressToLoc.put(addr, loc);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    public NodeType getNodeType(@Nonnull Location loc) { return nodes.get(loc); }
    public String getAddress(@Nonnull Location loc) { return addresses.get(loc); }
    @javax.annotation.Nullable
    public Location resolveAddress(@Nonnull String address) { return addressToLoc.get(address); }

    public void saveStorage(@Nonnull Location loc, Material mat, long amount) {
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        pdc.set(Keys.networkStorageMaterial(loc), PersistentDataType.STRING, mat == null ? "" : mat.name());
        pdc.set(Keys.networkStorageAmount(loc), PersistentDataType.LONG, amount);
    }

    public Material loadStorageMaterial(@Nonnull Location loc) {
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        String s = pdc.get(Keys.networkStorageMaterial(loc), PersistentDataType.STRING);
        if (s == null || s.isEmpty()) return null;
        try { return Material.valueOf(s); } catch (Exception e) { return null; }
    }

    public long loadStorageAmount(@Nonnull Location loc) {
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        Long v = pdc.get(Keys.networkStorageAmount(loc), PersistentDataType.LONG);
        return v == null ? 0L : v;
    }

    public void saveInserterFilter(@Nonnull Location loc, Material mat) {
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        pdc.set(Keys.networkInserterFilter(loc), PersistentDataType.STRING, mat == null ? "" : mat.name());
    }

    public Material loadInserterFilter(@Nonnull Location loc) {
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        String s = pdc.get(Keys.networkInserterFilter(loc), PersistentDataType.STRING);
        if (s == null || s.isEmpty()) return null;
        try { return Material.valueOf(s); } catch (Exception e) { return null; }
    }

    public int getStorageCapacity(@Nonnull Location loc) {
        PersistentDataContainer pdc = loc.getChunk().getPersistentDataContainer();
        Integer v = pdc.get(Keys.networkStorageCapacity(loc), PersistentDataType.INTEGER);
        return v == null ? 1000 : v;
    }

    public long depositFromPlayer(@Nonnull Location loc, @Nonnull org.bukkit.entity.Player player) {
        Material stored = loadStorageMaterial(loc);
        long amount = loadStorageAmount(loc);
        int capacity = getStorageCapacity(loc);
        org.bukkit.inventory.PlayerInventory pinv = player.getInventory();
        long moved = 0;
        for (org.bukkit.inventory.ItemStack it : pinv.getContents()) {
            if (it == null || it.getType() == Material.AIR) continue;
            if (stored == null) stored = it.getType();
            if (it.getType() != stored) continue;
            int can = capacity - (int)amount;
            if (can <= 0) break;
            int take = Math.min(it.getAmount(), can);
            if (take <= 0) continue;
            it.setAmount(it.getAmount() - take);
            amount += take;
            moved += take;
        }
        saveStorage(loc, stored, amount);
        return moved;
    }

    public int withdrawToPlayer(@Nonnull Location loc, @Nonnull org.bukkit.entity.Player player, int req) {
        Material stored = loadStorageMaterial(loc);
        long amount = loadStorageAmount(loc);
        if (stored == null || amount <= 0 || req <= 0) return 0;
        int toGive = (int)Math.min(amount, req);
        int given = 0;
        while (toGive > 0) {
            int stack = Math.min(64, toGive);
            org.bukkit.inventory.ItemStack stackItem = new org.bukkit.inventory.ItemStack(stored, stack);
            java.util.Map<Integer, org.bukkit.inventory.ItemStack> leftover = player.getInventory().addItem(stackItem);
            if (leftover.isEmpty()) {
                given += stack;
                toGive -= stack;
            } else {
                int notAdded = leftover.values().stream().mapToInt(org.bukkit.inventory.ItemStack::getAmount).sum();
                int actuallyAdded = stack - notAdded;
                given += actuallyAdded;
                toGive -= actuallyAdded;
                break;
            }
        }
        if (given > 0) {
            saveStorage(loc, stored, amount - given);
        }
        return given;
    }

    public java.util.List<StorageInfo> listStorages() {
        java.util.List<StorageInfo> list = new java.util.ArrayList<>();
        for (Map.Entry<Location, NodeType> e : nodes.entrySet()) {
            if (e.getValue() == NodeType.STORAGE) {
                Location loc = e.getKey();
                list.add(new StorageInfo(loc, getAddress(loc), loadStorageMaterial(loc), loadStorageAmount(loc)));
            }
        }
        return list;
    }

    public boolean areConnected(@Nonnull Location from, @Nonnull Location to) {
        if (from.getWorld() == null || !from.getWorld().equals(to.getWorld())) return false;
        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.ArrayDeque<Location> dq = new java.util.ArrayDeque<>();
        dq.add(from);
        visited.add(key(from));
        while (!dq.isEmpty()) {
            Location cur = dq.poll();
            if (cur.getBlockX() == to.getBlockX() && cur.getBlockY() == to.getBlockY() && cur.getBlockZ() == to.getBlockZ()) return true;
            for (Location n : neighbors(cur)) {
                if (visited.add(key(n)) && isTube(n)) dq.add(n);
            }
        }
        return false;
    }

    private java.util.List<Location> neighbors(Location loc) {
        java.util.List<Location> ns = new java.util.ArrayList<>(6);
        ns.add(new Location(loc.getWorld(), loc.getBlockX()+1, loc.getBlockY(), loc.getBlockZ()));
        ns.add(new Location(loc.getWorld(), loc.getBlockX()-1, loc.getBlockY(), loc.getBlockZ()));
        ns.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY()+1, loc.getBlockZ()));
        ns.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ()));
        ns.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()+1));
        ns.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()-1));
        return ns;
    }

    private boolean isTube(Location loc) {
        return loc.getBlock().getType() == Material.WHITE_STAINED_GLASS || nodes.containsKey(loc);
    }

    private String key(Location l) { return l.getBlockX()+":"+l.getBlockY()+":"+l.getBlockZ(); }
    private String computeAddress(Location l) { return "A-"+l.getBlockX()+"-"+l.getBlockY()+"-"+l.getBlockZ(); }

    public boolean isControllerPowered(@Nonnull Location loc) {
        if (!net.guizhanss.molecularfoundry.util.Config.isNetworkPowerCheckEnabled()) return true;
        var em = MolecularFoundry.getInstance().getEnergyManager();
        var storage = em.getStorage(loc);
        if (storage == null) return false;
        return storage.getEnergy() >= net.guizhanss.molecularfoundry.util.Config.getControllerTickCost();
    }

    public boolean canPerformNetworkOperation(@Nonnull Location controllerLoc) {
        if (!net.guizhanss.molecularfoundry.util.Config.isNetworkPowerCheckEnabled()) return true;
        var em = MolecularFoundry.getInstance().getEnergyManager();
        var storage = em.getStorage(controllerLoc);
        if (storage == null) return false;
        return storage.getEnergy() >= net.guizhanss.molecularfoundry.util.Config.getNetworkOperationCost();
    }

    public void chargeNetworkOperation(@Nonnull Location controllerLoc) {
        if (!net.guizhanss.molecularfoundry.util.Config.isNetworkPowerCheckEnabled()) return;
        var em = MolecularFoundry.getInstance().getEnergyManager();
        var storage = em.getStorage(controllerLoc);
        if (storage != null) storage.removeEnergy(net.guizhanss.molecularfoundry.util.Config.getNetworkOperationCost());
    }

    public static final class StorageInfo {
        public final Location location;
        public final String address;
        public final Material material;
        public final long amount;
        public StorageInfo(Location location, String address, Material material, long amount) {
            this.location = location; this.address = address; this.material = material; this.amount = amount;
        }
    }
}
