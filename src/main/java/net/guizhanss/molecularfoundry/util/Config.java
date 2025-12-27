package net.guizhanss.molecularfoundry.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import net.guizhanss.molecularfoundry.MolecularFoundry;

public class Config {
    private static YamlConfiguration config;
    private static File configFile;

    public static void load() {
        try {
            File dataFolder = MolecularFoundry.getInstance().getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();
            configFile = new File(dataFolder, "config.yml");
            if (!configFile.exists()) {
                configFile.createNewFile();
                config = new YamlConfiguration();
                // Set defaults
                config.set("power.controller-tick-cost", 2);
                config.set("power.network-operation-cost", 5);
                config.set("power.enable-network-power-check", true);
                // Machines behavior
                config.set("machines.require-machine-items", true);
                config.set("machines.allow-vanilla-conversion", false);
                config.save(configFile);
            } else {
                config = YamlConfiguration.loadConfiguration(configFile);
                // Ensure defaults exist
                if (!config.contains("power.controller-tick-cost")) config.set("power.controller-tick-cost", 2);
                if (!config.contains("power.network-operation-cost")) config.set("power.network-operation-cost", 5);
                if (!config.contains("power.enable-network-power-check")) config.set("power.enable-network-power-check", true);
                if (!config.contains("machines.require-machine-items")) config.set("machines.require-machine-items", true);
                if (!config.contains("machines.allow-vanilla-conversion")) config.set("machines.allow-vanilla-conversion", false);
                config.save(configFile);
            }
            MolecularFoundry.getInstance().getLogger().info("Config loaded.");
        } catch (IOException e) {
            MolecularFoundry.getInstance().getLogger().severe("Failed to load config: " + e.getMessage());
        }
    }

    public static int getControllerTickCost() {
        return config.getInt("power.controller-tick-cost", 2);
    }

    public static int getNetworkOperationCost() {
        return config.getInt("power.network-operation-cost", 5);
    }

    public static boolean isNetworkPowerCheckEnabled() {
        return config.getBoolean("power.enable-network-power-check", true);
    }

    public static boolean requireMachineItems() {
        return config.getBoolean("machines.require-machine-items", true);
    }

    public static boolean allowVanillaConversion() {
        return config.getBoolean("machines.allow-vanilla-conversion", false);
    }

    public static void reload() {
        load();
    }
}
