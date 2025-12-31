package net.thedreamty.doorKnock.metrics;

import net.thedreamty.doorKnock.DoorKnock;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;

import java.util.HashMap;
import java.util.Map;

public class PluginMetrics {
    
    private final DoorKnock plugin;
    private final Metrics metrics;
    private int totalKnocks = 0;
    private final Map<String, Integer> doorTypeUsage = new HashMap<>();
    private final Map<String, Integer> knockTypeUsage = new HashMap<>();
    
    public PluginMetrics(DoorKnock plugin) {
        this.plugin = plugin;
        int pluginId = 28621;
        
        if (pluginId <= 0) {
            plugin.getLogger().warning("Invalid bStats plugin ID: " + pluginId);
            this.metrics = null;
            return;
        }
        
        this.metrics = new Metrics(plugin, pluginId);
        setupCharts();
    }
    
    private void setupCharts() {
        if (metrics == null) return;
        
        metrics.addCustomChart(new SimplePie("plugin_version", () -> 
            plugin.getPluginMeta().getVersion()));
        
        metrics.addCustomChart(new SimplePie("server_version", () -> 
            plugin.getServer().getBukkitVersion()));
        
        metrics.addCustomChart(new SimplePie("server_online", () -> {
            int online = plugin.getServer().getOnlinePlayers().size();
            if (online == 0) return "0";
            if (online <= 5) return "1-5";
            if (online <= 10) return "6-10";
            if (online <= 20) return "11-20";
            if (online <= 50) return "21-50";
            return "50+";
        }));
        
        metrics.addCustomChart(new SingleLineChart("total_knocks", () -> {
            int count = totalKnocks;
            totalKnocks = 0;
            return count;
        }));
        
        metrics.addCustomChart(new AdvancedPie("popular_doors", () -> {
            Map<String, Integer> copy = new HashMap<>(doorTypeUsage);
            doorTypeUsage.clear();
            return copy;
        }));
        
        metrics.addCustomChart(new DrilldownPie("knock_types", () -> {
            Map<String, Map<String, Integer>> drilldown = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>(knockTypeUsage);
            
            if (!entry.isEmpty()) {
                drilldown.put("knock_types", entry);
                knockTypeUsage.clear();
            }
            
            return drilldown;
        }));
        
        metrics.addCustomChart(new SimplePie("max_hear_distance", () -> {
            double distance = plugin.getConfig().getDouble("distance_settings.max_hear_distance", 16.0);
            return String.valueOf((int) distance) + " blocks";
        }));
        
        metrics.addCustomChart(new DrilldownPie("enabled_features", () -> {
            Map<String, Map<String, Integer>> features = new HashMap<>();
            Map<String, Integer> enabled = new HashMap<>();
            
            enabled.put("soft_knock", plugin.getConfig().contains("knock_variations.soft_knock") ? 1 : 0);
            enabled.put("persistent_knock", plugin.getConfig().contains("knock_variations.persistent_knock") ? 1 : 0);
            enabled.put("debug_logging", plugin.getConfig().getBoolean("debug.log_knocks", false) ? 1 : 0);
            
            features.put("features", enabled);
            return features;
        }));
    }
    
    public void addKnockStat(String doorType, String knockType) {
        if (metrics == null) return;
        totalKnocks++;
        doorTypeUsage.merge(doorType, 1, Integer::sum);
        knockTypeUsage.merge(knockType, 1, Integer::sum);
    }
    
    public void addReloadStat() {
        if (metrics == null) return;
        metrics.addCustomChart(new SingleLineChart("config_reloads", () -> 1));
    }
}
