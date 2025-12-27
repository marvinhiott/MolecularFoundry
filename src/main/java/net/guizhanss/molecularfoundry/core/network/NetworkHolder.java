package net.guizhanss.molecularfoundry.core.network;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

// Simple holder to carry block location for network GUIs.
public class NetworkHolder implements InventoryHolder {
    private final Location location;
    private final NetworkManager.NodeType type;

    public NetworkHolder(Location location, NetworkManager.NodeType type) {
        this.location = location;
        this.type = type;
    }

    public Location getLocation() { return location; }
    public NetworkManager.NodeType getType() { return type; }

    @Override
    public Inventory getInventory() { return null; }
}
