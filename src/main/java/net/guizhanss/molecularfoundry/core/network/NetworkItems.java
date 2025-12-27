package net.guizhanss.molecularfoundry.core.network;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.guizhanss.molecularfoundry.util.Keys;

import java.util.List;

public class NetworkItems {
    public static ItemStack createTransportTube() {
        return createNetworkItem(Material.WHITE_STAINED_GLASS, "Transport Tube", "network_tube", 1);
    }

    public static ItemStack createInserter() {
        return createNetworkItem(Material.BLUE_STAINED_GLASS, "Network Inserter", "network_inserter", 1);
    }

    public static ItemStack createGetter() {
        return createNetworkItem(Material.YELLOW_STAINED_GLASS, "Network Getter", "network_getter", 1);
    }

    public static ItemStack createController() {
        return createNetworkItem(Material.REDSTONE_LAMP, "Network Controller", "network_controller", 1);
    }

    public static ItemStack createJukeboxController() {
        return createNetworkItem(Material.JUKEBOX, "Network Controller (Jukebox)", "network_jukebox", 1);
    }

    public static ItemStack createStorage1k() {
        return createNetworkItem(Material.TERRACOTTA, "Network Storage (1k)", "network_storage_1k", 1);
    }

    public static ItemStack createStorage5k() {
        return createNetworkItem(Material.BLUE_TERRACOTTA, "Network Storage (5k)", "network_storage_5k", 1);
    }

    public static ItemStack createStorage10k() {
        return createNetworkItem(Material.CYAN_TERRACOTTA, "Network Storage (10k)", "network_storage_10k", 1);
    }

    private static ItemStack createNetworkItem(Material material, String displayName, String typeId, int amount) {
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + displayName);
            meta.setLore(List.of(ChatColor.GRAY + "Place to create a network node.", ChatColor.DARK_GRAY + "Type: " + typeId));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            meta.getPersistentDataContainer().set(Keys.networkItemType(), PersistentDataType.STRING, typeId);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
