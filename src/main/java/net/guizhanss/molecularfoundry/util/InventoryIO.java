package net.guizhanss.molecularfoundry.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

// Utility to serialize and deserialize inventory contents for persistence.
public final class InventoryIO {
    private InventoryIO() {}

    public static String itemArrayToBase64(ItemStack[] items) {
        if (items == null) return "";
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            return "";
        }
    }

    public static ItemStack[] itemArrayFromBase64(String data) {
        if (data == null || data.isEmpty()) return new ItemStack[0];
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                try {
                    items[i] = (ItemStack) dataInput.readObject();
                } catch (ClassNotFoundException e) {
                    items[i] = null;
                }
            }
            return items;
        } catch (IOException e) {
            return new ItemStack[0];
        }
    }
}
