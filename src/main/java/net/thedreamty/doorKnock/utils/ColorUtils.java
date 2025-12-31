package net.thedreamty.doorKnock.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public class ColorUtils {
    
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();
    
    public static Component parse(String text) {
        if (text == null) return Component.empty();
        
        try {
            String processed = text;
            
            if (processed.contains("<#")) {
                processed = processed.replaceAll("<#([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])>", "§x§$1§$2§$3§$4§$5§$6");
            }
            processed = processed
                .replaceAll("<black>", "§0")
                .replaceAll("<dark_blue>", "§1")
                .replaceAll("<dark_green>", "§2")
                .replaceAll("<dark_aqua>", "§3")
                .replaceAll("<dark_red>", "§4")
                .replaceAll("<dark_purple>", "§5")
                .replaceAll("<gold>", "§6")
                .replaceAll("<gray>", "§7")
                .replaceAll("<dark_gray>", "§8")
                .replaceAll("<blue>", "§9")
                .replaceAll("<green>", "§a")
                .replaceAll("<aqua>", "§b")
                .replaceAll("<red>", "§c")
                .replaceAll("<light_purple>", "§d")
                .replaceAll("<yellow>", "§e")
                .replaceAll("<white>", "§f")
                .replaceAll("<reset>", "§r")
                .replaceAll("<bold>", "§l")
                .replaceAll("<italic>", "§o")
                .replaceAll("<underline>", "§n")
                .replaceAll("<strikethrough>", "§m");
            
            return legacySerializer.deserialize(processed.replace('&', '§'));
            
        } catch (Exception e) {
            return legacySerializer.deserialize(text.replace('&', '§'));
        }
    }
    
    public static void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(parse(text));
    }
}
