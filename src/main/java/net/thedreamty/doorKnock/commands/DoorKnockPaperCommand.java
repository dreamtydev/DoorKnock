package net.thedreamty.doorKnock.commands;

import net.thedreamty.doorKnock.DoorKnock;
import net.thedreamty.doorKnock.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DoorKnockPaperCommand extends Command implements TabCompleter {
    
    public DoorKnockPaperCommand() {
        super("doorknock", "DoorKnock plugin commands", "/doorknock reload", List.of("dk", "knock"));
        setPermission("doorknock.admin");
    }
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.hasPermission("doorknock.admin")) {
            String noPermMsg = DoorKnock.getInstance().getConfig().getString("messages.no_permission", 
                    "<#6eff94>✂ &7» &cYou don't have permission to use this command!");
            ColorUtils.sendMessage(sender, noPermMsg);
            return true;
        }
        
        if (args.length == 0) {
            ColorUtils.sendMessage(sender, "<#6eff94>✂ &7» &fDoorKnock Commands:");
            ColorUtils.sendMessage(sender, "<#6eff94>✂ &7» <#6eff94>/doorknock reload &f- Reload configuration");
            ColorUtils.sendMessage(sender, "<#6eff94>✂ &7» <#6eff94>/dk reload &f- Reload configuration (alias)");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            DoorKnock.getInstance().reloadPluginConfig();
            String reloadMsg = DoorKnock.getInstance().getConfig().getString("messages.reload", 
                    "<#6eff94>✂ &7» &fConfiguration reloaded successfully!");
            ColorUtils.sendMessage(sender, reloadMsg);
            return true;
        }
        
        ColorUtils.sendMessage(sender, "<#6eff94>✂ &7» &cUnknown subcommand. Use /doorknock for help.");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }
        
        return Collections.emptyList();
    }
}
