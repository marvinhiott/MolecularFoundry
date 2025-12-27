package net.guizhanss.molecularfoundry.core.processing;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Minimal stub reserved for future machines. Currently a no-op ticker.
 */
public class MachineTicker {
    private final Plugin plugin;
    private BukkitTask task;

    public MachineTicker(Plugin plugin) { this.plugin = plugin; }

    public void registerMachine(Block block, String type) { /* reserved for future machines */ }
    public void unregisterMachine(Location loc) { /* reserved for future machines */ }

    public void runTaskTimer(Plugin plugin, long delay, long period) { start(); }

    public void start() {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // No-op until future machines use this ticker
        }, 20L, 20L);
    }

    public void saveAll() { /* reserved for future persistence */ }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }
}