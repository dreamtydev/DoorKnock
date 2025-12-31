package net.thedreamty.doorKnock.listeners;

import net.thedreamty.doorKnock.DoorKnock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DoorKnockListener implements Listener {
    
    private final Map<UUID, Long> lastKnockTime = new HashMap<>();
    private final Map<UUID, Integer> consecutiveKnocks = new HashMap<>();
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null || !(block.getBlockData() instanceof Door)) return;
        
        Player player = event.getPlayer();
        
        if (!player.hasPermission("doorknock.knock")) {
            if (DoorKnock.getInstance().getConfig().getBoolean("debug.log_knocks", false)) {
                DoorKnock.getInstance().getLogger().info(player.getName() + " tried to knock but lacks permission");
            }
            return;
        }
        
        KnockType knockType = determineKnockType(player);
        
        playKnockSound(player, block, knockType);
    }
    
    private KnockType determineKnockType(Player player) {
        if (player.isSneaking()) {
            return KnockType.SOFT;
        }
        
        return KnockType.NORMAL;
    }
    
        
    private void playKnockSound(Player player, Block doorBlock, KnockType knockType) {
        try {
            Door door = (Door) doorBlock.getBlockData();
            String doorMaterial = getDoorMaterialName(door.getMaterial());
            
            String soundPath = "door_sounds." + doorMaterial + "." + getKnockTypeSuffix(knockType);
            String soundName = DoorKnock.getInstance().getConfig().getString(soundPath);
            
            if (soundName == null) {
                soundPath = "door_sounds." + doorMaterial + ".normal";
                soundName = DoorKnock.getInstance().getConfig().getString("door_sounds." + doorMaterial + "." + soundPath, "block.wood.hit");
            }
            
            DoorKnock.getInstance().getLogger().info("Trying to play sound: " + soundName + " for material: " + doorMaterial);
            
            Sound sound = null;
            try {
                NamespacedKey key = NamespacedKey.minecraft(soundName.toLowerCase());
                sound = Registry.SOUNDS.get(key);
                if (sound == null) {
                    key = NamespacedKey.minecraft(soundName.replace('.', '_').toLowerCase());
                    sound = Registry.SOUNDS.get(key);
                }
            } catch (Exception e) {
                DoorKnock.getInstance().getLogger().warning("Failed to load sound: " + soundName + ", using fallback");
                sound = Sound.BLOCK_WOOD_HIT;
            }
            
            if (sound == null) {
                DoorKnock.getInstance().getLogger().warning("Sound not found: " + soundName + ", using fallback");
                sound = Sound.BLOCK_WOOD_HIT;
            }
            
            double volume = DoorKnock.getInstance().getConfig()
                    .getDouble("door_sounds." + doorMaterial + ".volume", 1.0);
            double pitch = DoorKnock.getInstance().getConfig()
                    .getDouble("door_sounds." + doorMaterial + ".pitch", 1.0);
            
            String variationPath = "knock_variations." + getKnockTypeConfigKey(knockType);
            volume *= DoorKnock.getInstance().getConfig()
                    .getDouble(variationPath + ".volume_multiplier", 1.0);
            pitch *= DoorKnock.getInstance().getConfig()
                    .getDouble(variationPath + ".pitch_multiplier", 1.0);
            
            playSoundForNearbyPlayers(doorBlock.getLocation(), sound, (float) volume, (float) pitch, player);
            
            if (DoorKnock.getInstance().getMetrics() != null) {
                DoorKnock.getInstance().getMetrics().addKnockStat(doorMaterial, knockType.name());
            }
            
            if (DoorKnock.getInstance().getConfig().getBoolean("debug.log_knocks", false)) {
                DoorKnock.getInstance().getLogger().info(
                        player.getName() + " knocked on " + doorMaterial + " door (" + knockType + ")"
                );
            }
            
        } catch (Exception e) {
            DoorKnock.getInstance().getLogger().warning("Error playing knock sound: " + e.getMessage());
        }
    }
    
    private void playSoundForNearbyPlayers(Location location, Sound sound, float volume, float pitch, Player knocker) {
        double maxDistance = DoorKnock.getInstance().getConfig()
                .getDouble("distance_settings.max_hear_distance", 16.0);
        double falloff = DoorKnock.getInstance().getConfig()
                .getDouble("distance_settings.volume_falloff", 0.1);
        
        for (Player player : location.getWorld().getPlayers()) {
            if (!player.hasPermission("doorknock.hear")) continue;
            
            double distance = player.getLocation().distance(location);
            if (distance > maxDistance) continue;
            
            float playerVolume = (float) Math.max(0.1, volume * (1.0 - (distance * falloff)));
            
            player.playSound(location, sound, playerVolume, pitch);
        }
    }
    
    private String getDoorMaterialName(Material material) {
        String name = material.name().toLowerCase();
        DoorKnock.getInstance().getLogger().info("Door material detected: " + name);
        
        if (name.contains("oak")) return "oak";
        if (name.contains("spruce")) return "spruce";
        if (name.contains("birch")) return "birch";
        if (name.contains("jungle")) return "jungle";
        if (name.contains("acacia")) return "acacia";
        if (name.contains("dark_oak")) return "dark_oak";
        if (name.contains("mangrove")) return "mangrove";
        if (name.contains("cherry")) return "cherry";
        if (name.contains("bamboo")) return "bamboo";
        if (name.contains("crimson")) return "crimson";
        if (name.contains("warped")) return "warped";
        if (name.contains("copper")) return "copper";
        if (name.contains("pale")) return "pale";
        if (name.contains("iron")) return "iron";
        return "oak"; // Default fallback
    }
    
    private String getKnockTypeSuffix(KnockType knockType) {
        return switch (knockType) {
            case NORMAL -> "normal";
            case SOFT -> "soft";
        };
    }
    
    private String getKnockTypeConfigKey(KnockType knockType) {
        return switch (knockType) {
            case NORMAL -> "normal";
            case SOFT -> "soft_knock";
        };
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        lastKnockTime.remove(playerId);
        consecutiveKnocks.remove(playerId);
    }
    
    private enum KnockType {
        NORMAL, SOFT
    }
}
