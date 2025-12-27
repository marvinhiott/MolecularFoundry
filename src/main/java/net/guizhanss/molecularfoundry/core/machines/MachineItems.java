package net.guizhanss.molecularfoundry.core.machines;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.guizhanss.molecularfoundry.util.Keys;

import java.util.List;

public class MachineItems {
    public static ItemStack create(MachineType type) {
        ItemStack stack = new ItemStack(type.baseMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + type.displayName());
            meta.setLore(List.of(ChatColor.GRAY + "Place to set up a machine.", ChatColor.DARK_GRAY + "Type: " + type.id()));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            meta.getPersistentDataContainer().set(Keys.machineItemType(), PersistentDataType.STRING, type.id());
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
