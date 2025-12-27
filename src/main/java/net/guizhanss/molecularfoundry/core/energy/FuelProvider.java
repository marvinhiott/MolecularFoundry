package net.guizhanss.molecularfoundry.core.energy;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class FuelProvider extends EnergyProvider {
    private int burnTime = 0;
    private int maxBurnTime = 0;

    public FuelProvider(@Nonnull Location location, int rate) {
        super(location, rate);
    }

    @Override
    public int generateEnergy() {
        if (burnTime > 0) {
            burnTime--;
            lastGenerated = energyPerTick;
            return energyPerTick;
        }
        
        // Try to consume fuel
        if (location.getBlock().getState() instanceof Furnace furnace) {
            ItemStack fuel = furnace.getInventory().getFuel();
            if (fuel != null && fuel.getType() != Material.AIR) {
                int burnTicks = getBurnTime(fuel.getType());
                if (burnTicks > 0) {
                    fuel.setAmount(fuel.getAmount() - 1);
                    furnace.getInventory().setFuel(fuel);
                    furnace.update();
                    burnTime = burnTicks;
                    maxBurnTime = burnTicks;
                    lastGenerated = energyPerTick;
                    return energyPerTick;
                }
            }
        }
        lastGenerated = 0;
        return 0;
    }

    private int getBurnTime(Material material) {
        return switch (material) {
            case COAL, CHARCOAL -> 1600; // 80 seconds
            case COAL_BLOCK -> 16000; // 800 seconds
            case LAVA_BUCKET -> 20000; // 1000 seconds
            case BLAZE_ROD -> 2400; // 120 seconds
            case STICK -> 100; // 5 seconds
            default -> {
                String name = material.name();
                if (name.contains("PLANKS") || name.contains("LOG") || name.contains("WOOD")) {
                    yield 300;
                }
                yield 0;
            }
        };
    }

    public int getBurnTime() { return burnTime; }
    public int getMaxBurnTime() { return maxBurnTime; }
    public int getBurnPercent() { return maxBurnTime > 0 ? (burnTime * 100 / maxBurnTime) : 0; }
}
