package net.guizhanss.molecularfoundry.core.energy;

import org.bukkit.scheduler.BukkitRunnable;

import net.guizhanss.molecularfoundry.MolecularFoundry;

public class EnergyTicker extends BukkitRunnable {
    private final EnergyManager manager;
    public EnergyTicker(EnergyManager m) { this.manager = m; }
    @Override public void run() { manager.tickProviders(); }
    public void start() { this.runTaskTimer(MolecularFoundry.getInstance(), 1L, 1L); }
}
