package net.guizhanss.molecularfoundry.core.machines;

import org.bukkit.Material;

public enum MachineType {
    SOLAR_GENERATOR("solar", "Solar Generator", Material.DAYLIGHT_DETECTOR),
    COAL_GENERATOR("coal_generator", "Coal Generator", Material.FURNACE),
    MOLECULAR_SYNTHESIZER("molecular_synthesizer", "Molecular Synthesizer", Material.BLAST_FURNACE),
    RECOMBINATOR("recombinator", "Blueprint Recombinator", Material.BREWING_STAND);

    private final String id;
    private final String display;
    private final Material baseMaterial;

    MachineType(String id, String display, Material baseMaterial) {
        this.id = id;
        this.display = display;
        this.baseMaterial = baseMaterial;
    }

    public String id() { return id; }
    public String displayName() { return display; }
    public Material baseMaterial() { return baseMaterial; }

    public static MachineType fromId(String id) {
        for (MachineType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }
}
