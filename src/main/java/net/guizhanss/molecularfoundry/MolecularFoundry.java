package net.guizhanss.molecularfoundry;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.guizhanss.molecularfoundry.core.energy.EnergyManager;
import net.guizhanss.molecularfoundry.core.energy.EnergyTicker;
import net.guizhanss.molecularfoundry.core.listeners.BlockListener;
import net.guizhanss.molecularfoundry.core.processing.MachineTicker;
import net.guizhanss.molecularfoundry.core.processing.RecombinatorTicker;
import net.guizhanss.molecularfoundry.core.network.NetworkManager;
import net.guizhanss.molecularfoundry.items.GeneticBlueprint;
import net.guizhanss.molecularfoundry.core.machines.MachineType;
import net.guizhanss.molecularfoundry.core.machines.MachineItems;

public class MolecularFoundry extends JavaPlugin {

    private static MolecularFoundry instance;

    private EnergyManager energyManager;
    private EnergyTicker energyTicker;
    private MachineTicker machineTicker;
    private net.guizhanss.molecularfoundry.core.processing.SynthesizerTicker synthesizerTicker;
    private RecombinatorTicker recombinatorTicker;
    private NetworkManager networkManager;

    public static MolecularFoundry getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().log(Level.INFO, "Molecular Foundry enabling...");

        net.guizhanss.molecularfoundry.util.Config.load();

        energyManager = new EnergyManager();
        energyManager.bootstrapFromLoadedChunks();
        energyTicker = new EnergyTicker(energyManager);
        energyTicker.start();

        networkManager = new NetworkManager();
        networkManager.bootstrapFromLoadedChunks();

        machineTicker = new MachineTicker(this);
        machineTicker.runTaskTimer(this, 1L, 1L);
        synthesizerTicker = new net.guizhanss.molecularfoundry.core.processing.SynthesizerTicker();
        synthesizerTicker.runTaskTimer(this, 1L, 1L);
        recombinatorTicker = new RecombinatorTicker();
        recombinatorTicker.start();

        getServer().getPluginManager().registerEvents(new BlockListener(), this);

        PluginCommand cmd = getCommand("molf");
        if (cmd != null) {
            cmd.setExecutor((sender, command, label, args) -> {
                if (args.length == 0) {
                    sender.sendMessage("\u00a7aMolecular Foundry is online.");
                    sender.sendMessage("\u00a77/molf blueprint <material> - Get a genetic blueprint");
                    sender.sendMessage("\u00a77/molf netcheck <addrA> <addrB> - Check tube connectivity");
                    sender.sendMessage("\u00a77/molf give <machine> - Get a machine block");
                    sender.sendMessage("\u00a77/molf menu - Open plugin menu (machines/network)");
                    return true;
                }

                if (args[0].equalsIgnoreCase("blueprint")) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("\u00a7cOnly players can use this command.");
                        return true;
                    }

                    if (args.length < 2) {
                        sender.sendMessage("\u00a7cUsage: /molf blueprint <material>");
                        return true;
                    }

                    Material target;
                    try {
                        target = Material.valueOf(args[1].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("\u00a7cInvalid material: " + args[1]);
                        return true;
                    }

                    if (!target.isItem()) {
                        sender.sendMessage("\u00a7c" + target + " is not a valid item material.");
                        return true;
                    }

                    player.getInventory().addItem(GeneticBlueprint.create(target, null));
                    sender.sendMessage("\u00a7aGave genetic blueprint for " + target.name());
                    return true;
                } else if (args[0].equalsIgnoreCase("netcheck")) {
                    if (args.length < 3) { sender.sendMessage("\u00a7cUsage: /molf netcheck <addrA> <addrB>"); return true; }
                    var nm = getNetworkManager();
                    org.bukkit.Location a = nm.resolveAddress(args[1]);
                    org.bukkit.Location b = nm.resolveAddress(args[2]);
                    if (a == null) { sender.sendMessage("\u00a7cUnknown address: " + args[1]); return true; }
                    if (b == null) { sender.sendMessage("\u00a7cUnknown address: " + args[2]); return true; }
                    boolean ok = nm.areConnected(a, b);
                    sender.sendMessage(ok ? "\u00a7aConnected via tubes" : "\u00a7cNot connected");
                    return true;
                } else if (args[0].equalsIgnoreCase("menu")) {
                    if (!(sender instanceof Player player)) { sender.sendMessage("\u00a7cOnly players can open the menu."); return true; }
                    net.guizhanss.molecularfoundry.core.ui.PluginMenu.openMain(player);
                    return true;
                } else if (args[0].equalsIgnoreCase("addrhere")) {
                    if (!(sender instanceof Player player)) { sender.sendMessage("\u00a7cOnly players"); return true; }
                    org.bukkit.block.Block target = player.getTargetBlockExact(6);
                    if (target == null) { sender.sendMessage("\u00a7cLook at a node within 6 blocks"); return true; }
                    var nm = getNetworkManager();
                    String addr = nm.getAddress(target.getLocation());
                    if (addr == null) { sender.sendMessage("\u00a7eUnregistered node"); return true; }
                    sender.sendMessage("\u00a77Address: \u00a7b" + addr);
                    return true;
                } else if (args[0].equalsIgnoreCase("give")) {
                    if (!(sender instanceof Player player)) { sender.sendMessage("\u00a7cOnly players can use this command."); return true; }
                    if (args.length < 2) { sender.sendMessage("\u00a7cUsage: /molf give <solar|coal|synth|recomb>"); return true; }
                    String which = args[1].toLowerCase();
                    MachineType type = switch (which) {
                        case "solar", "sol", "sun" -> MachineType.SOLAR_GENERATOR;
                        case "coal", "coalgen" -> MachineType.COAL_GENERATOR;
                        case "synth", "synthesizer" -> MachineType.MOLECULAR_SYNTHESIZER;
                        case "recomb", "recombinator" -> MachineType.RECOMBINATOR;
                        default -> null;
                    };
                    if (type == null) { sender.sendMessage("\u00a7cUnknown machine: " + which); return true; }
                    player.getInventory().addItem(MachineItems.create(type));
                    sender.sendMessage("\u00a7aGave: " + type.displayName());
                    return true;
                }

                sender.sendMessage("\u00a7cUnknown subcommand. Use /molf for help.");
                return true;
            });
        }
    }

    @Override
    public void onDisable() {
        if (energyManager != null) energyManager.saveAll();
        if (energyTicker != null) energyTicker.cancel();
        if (machineTicker != null) machineTicker.saveAll();
        if (recombinatorTicker != null) recombinatorTicker.stop();
    }

    public EnergyManager getEnergyManager() { return energyManager; }
    public MachineTicker getMachineTicker() { return machineTicker; }
    public net.guizhanss.molecularfoundry.core.processing.SynthesizerTicker getSynthesizerTicker() { return synthesizerTicker; }
    public RecombinatorTicker getRecombinatorTicker() { return recombinatorTicker; }
    public NetworkManager getNetworkManager() { return networkManager; }
}