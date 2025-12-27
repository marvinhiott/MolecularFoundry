package net.guizhanss.molecularfoundry.core.energy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.guizhanss.molecularfoundry.MolecularFoundry;
import net.guizhanss.molecularfoundry.util.Keys;

public class EnergyManager {
    private final Map<Location, EnergyStorage> storages = new ConcurrentHashMap<>();
    private final Map<Location, EnergyProvider> providers = new ConcurrentHashMap<>();

    public void registerStorage(@Nonnull Location loc, @Nonnull EnergyStorage storage) {
        storage.load(); storages.put(loc, storage);
    }
    public void registerProvider(@Nonnull Location loc, @Nonnull EnergyProvider provider) {
        providers.put(loc, provider);
    }
    public void unregister(@Nonnull Location loc) {
        EnergyStorage s = storages.remove(loc); if (s != null) s.save(); providers.remove(loc);
    }

    @Nullable public EnergyStorage getStorage(@Nonnull Location loc) { return storages.get(loc); }
    @Nullable public EnergyProvider getProvider(@Nonnull Location loc) { return providers.get(loc); }

    public void tickProviders() {
        for (Map.Entry<Location, EnergyProvider> e : providers.entrySet()) {
            Location ploc = e.getKey(); EnergyProvider p = e.getValue();
            int gen = p.generateEnergy();
            EnergyStorage ps = storages.get(ploc);
            if (ps != null) {
                // Store surplus first
                if (gen > 0) {
                    int leftover = distribute(ploc, gen);
                    if (leftover > 0) ps.addEnergy(leftover);
                } else {
                    int drained = ps.removeEnergy(ps.getCapacity());
                    if (drained > 0) distribute(ploc, drained);
                }
            }
        }
    }

    private int distribute(@Nonnull Location src, int amount) {
        int rem = amount;
        for (Map.Entry<Location, EnergyStorage> e : storages.entrySet()) {
            if (rem <= 0) break;
            Location dst = e.getKey(); EnergyStorage ms = e.getValue();
            if (!providers.containsKey(dst) && dst.getWorld().equals(src.getWorld()) && dst.distance(src) <= 16 && !ms.isFull()) {
                int can = ms.getCapacity() - ms.getEnergy();
                int xfer = Math.min(rem, can);
                ms.addEnergy(xfer); rem -= xfer;
            }
        }
        return rem;
    }

    public void bootstrapFromLoadedChunks() {
        for (org.bukkit.World world : MolecularFoundry.getInstance().getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) scanChunk(chunk);
        }
    }

    public void scanChunk(@Nonnull Chunk chunk) {
        var pdc = chunk.getPersistentDataContainer();
        for (NamespacedKey key : pdc.getKeys()) {
            String name = key.getKey();
            if (name.startsWith("mf_machine_")) {
                String type = pdc.get(key, PersistentDataType.STRING);
                if (type != null) {
                    try {
                        String[] parts = name.substring("mf_machine_".length()).split("_");
                        int x = Integer.parseInt(parts[0]); int y = Integer.parseInt(parts[1]); int z = Integer.parseInt(parts[2]);
                        Location loc = new Location(chunk.getWorld(), x, y, z);
                        registerFromType(loc.getBlock(), type);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private void registerFromType(@Nonnull org.bukkit.block.Block block, @Nonnull String type) {
        Location loc = block.getLocation(); if (storages.containsKey(loc)) return;
        int cap = switch (type) {
            case "solar" -> 500;
            case "coal_generator" -> 1000;
            case "molecular_synthesizer" -> 2000;
            case "recombinator" -> 2000;
            default -> 200;
        };
        registerStorage(loc, new EnergyStorage(loc, cap));
        if ("solar".equals(type)) registerProvider(loc, new EnergyProvider.Solar(loc, 5));
        if ("coal_generator".equals(type)) registerProvider(loc, new net.guizhanss.molecularfoundry.core.energy.FuelProvider(loc, 10));
        if ("recombinator".equals(type)) MolecularFoundry.getInstance().getRecombinatorTicker().registerMachine(block);
        if (!"solar".equals(type) && !"coal_generator".equals(type) && !"recombinator".equals(type)) {
            // Network nodes and others are ignored here
            MolecularFoundry.getInstance().getMachineTicker().registerMachine(block, type);
        }
    }

    public void saveAll() { for (EnergyStorage s : storages.values()) s.save(); }
}
