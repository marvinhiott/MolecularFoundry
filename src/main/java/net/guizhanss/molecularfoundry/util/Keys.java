package net.guizhanss.molecularfoundry.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import net.guizhanss.molecularfoundry.MolecularFoundry;

public class Keys {
    private static String keyFor(Location loc, String prefix) {
        String w = loc.getWorld() != null ? loc.getWorld().getName() : "world";
        return prefix+"_"+w+"_"+loc.getBlockX()+"_"+loc.getBlockY()+"_"+loc.getBlockZ();
    }
    public static NamespacedKey machineKey(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_machine")); }
    public static NamespacedKey energyKey(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_energy")); }
    public static NamespacedKey blueprintTarget() { return new NamespacedKey(MolecularFoundry.getInstance(), "mf_blueprint_target"); }
    public static NamespacedKey synthInventory(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_synth_inv")); }
    public static NamespacedKey synthProgress(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_synth_prog")); }
    public static NamespacedKey recombInventory(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_recomb_inv")); }
    public static NamespacedKey recombProgress(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_recomb_prog")); }
    public static NamespacedKey networkNode(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_net_node")); }
    public static NamespacedKey networkStorageMaterial(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_net_mat")); }
    public static NamespacedKey networkStorageAmount(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_net_amt")); }
    public static NamespacedKey networkInserterFilter(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_net_filter")); }
    public static NamespacedKey networkAddress(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_net_addr")); }
    public static NamespacedKey networkStorageCapacity(Location loc) { return new NamespacedKey(MolecularFoundry.getInstance(), keyFor(loc, "mf_net_cap")); }

    // Item keys (not location-based)
    public static NamespacedKey machineItemType() { return new NamespacedKey(MolecularFoundry.getInstance(), "mf_machine_item_type"); }
    public static NamespacedKey networkItemType() { return new NamespacedKey(MolecularFoundry.getInstance(), "mf_network_item_type"); }
}
