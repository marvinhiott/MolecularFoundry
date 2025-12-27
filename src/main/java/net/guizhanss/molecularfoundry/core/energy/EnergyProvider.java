package net.guizhanss.molecularfoundry.core.energy;

import org.bukkit.Location;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

public abstract class EnergyProvider {
    protected final Location location;
    protected final int energyPerTick;
    protected int lastGenerated;

    public EnergyProvider(@Nonnull Location loc, int ept) {
        this.location = loc; this.energyPerTick = ept; this.lastGenerated = 0;
    }
    public abstract int generateEnergy();
    public int getLastGenerated() { return lastGenerated; }

    public static class Solar extends EnergyProvider {
        public Solar(@Nonnull Location loc, int ept) { super(loc, ept); }
        @Override public int generateEnergy() {
            Block block = location.getBlock();
            long time = block.getWorld().getTime();
            boolean day = time < 12300 || time > 23850;
            if (!day) { lastGenerated = 0; return 0; }
            int highestY = block.getWorld().getHighestBlockYAt(location);
            if (location.getBlockY() < highestY - 1) { lastGenerated = 0; return 0; }
            if (block.getWorld().hasStorm()) { lastGenerated = energyPerTick/4; return lastGenerated; }
            lastGenerated = energyPerTick; return lastGenerated;
        }
    }
}
