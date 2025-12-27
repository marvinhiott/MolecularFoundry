package net.guizhanss.molecularfoundry.core.energy;

import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;

import net.guizhanss.molecularfoundry.util.Keys;

public class EnergyStorage {
    private final Location location;
    private int energy;
    private final int capacity;
    private final int maxInput;
    private final int maxOutput;

    public EnergyStorage(@Nonnull Location location, int capacity) {
        this(location, capacity, capacity/10, capacity/10);
    }

    public EnergyStorage(@Nonnull Location location, int capacity, int maxInput, int maxOutput) {
        this.location = location;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.energy = 0;
    }

    public int addEnergy(int amount) {
        int toAdd = Math.min(amount, Math.min(maxInput, capacity - energy));
        energy += toAdd;
        save();
        return toAdd;
    }

    public int removeEnergy(int amount) {
        int toRemove = Math.min(amount, Math.min(maxOutput, energy));
        energy -= toRemove;
        save();
        return toRemove;
    }

    public boolean hasEnergy(int amount) { return energy >= amount; }
    public boolean isFull() { return energy >= capacity; }
    public int getEnergy() { return energy; }
    public int getCapacity() { return capacity; }

    public void setEnergy(int e) { energy = Math.max(0, Math.min(e, capacity)); }

    public void save() {
        PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        pdc.set(Keys.energyKey(location), PersistentDataType.INTEGER, energy);
    }
    public void load() {
        PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        Integer v = pdc.get(Keys.energyKey(location), PersistentDataType.INTEGER);
        if (v != null) setEnergy(v);
    }
}
