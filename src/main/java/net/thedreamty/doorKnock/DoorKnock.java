package net.thedreamty.doorKnock;

import net.thedreamty.doorKnock.commands.DoorKnockPaperCommand;
import net.thedreamty.doorKnock.listeners.DoorKnockListener;
import net.thedreamty.doorKnock.metrics.PluginMetrics;
import net.thedreamty.doorKnock.utils.ColorUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class DoorKnock extends JavaPlugin {
    private static DoorKnock instance;
    private PluginMetrics metrics;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new DoorKnockListener(), this);
        
        try {
            getServer().getCommandMap().register("doorknock", new DoorKnockPaperCommand());
        } catch (Exception e) {
            getLogger().warning("Failed to register command: " + e.getMessage());
        }
        
        if (getConfig().getBoolean("metrics.enabled", true)) {
            metrics = new PluginMetrics(this);
        }
        
        getServer().getConsoleSender().sendMessage(ColorUtils.parse("<green>DoorKnock plugin enabled!"));
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ColorUtils.parse("<yellow>DoorKnock plugin disabled!"));
    }
    
    public static DoorKnock getInstance() {
        return instance;
    }
    
    public PluginMetrics getMetrics() {
        return metrics;
    }
    
    public void reloadPluginConfig() {
        reloadConfig();
        getServer().getConsoleSender().sendMessage(ColorUtils.parse("<yellow>Configuration reloaded!"));
        
        if (metrics != null) {
            metrics.addReloadStat();
        }
        
        getServer().getOnlinePlayers().stream()
            .filter(player -> player.hasPermission("doorknock.admin"))
            .forEach(player -> {
                String reloadMsg = getConfig().getString("messages.reload", 
                    "<#6eff94>✂ &7» &fConfiguration reloaded successfully!");
                player.sendMessage(ColorUtils.parse(reloadMsg));
            });
    }
}
