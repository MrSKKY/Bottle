package commands;

import fr.alexandre.iconicbottleexp.IconicBottleEXP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandsAdmin implements CommandExecutor {
   private IconicBottleEXP iconicBottleEXP;
   private CommandsBottle commandsBottle;

   public CommandsAdmin(IconicBottleEXP iconicBottleEXP) {
      this.iconicBottleEXP = iconicBottleEXP;
      this.commandsBottle = this.commandsBottle;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
         if (sender instanceof Player) {
            Player player = (Player)sender;
            String permission = this.iconicBottleEXP.getConfig().getString("permissions.admin-use");
            if (permission != null && !player.hasPermission(permission)) {
               sender.sendMessage(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("messages.no-permission").replace("&", "§")));
               return true;
            }
         }

         sender.sendMessage(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("commands-messages.admin.reload").replace("&", "§")));
         this.iconicBottleEXP.reloadConfig();
         this.iconicBottleEXP.loadLanguageFile(this.iconicBottleEXP.getConfig().getString("options.locale", this.iconicBottleEXP.getLanguage()));
         return true;
      } else if (args.length > 0 && args[0].equalsIgnoreCase("version")) {
         sender.sendMessage("§fPlugin version: §b" + this.iconicBottleEXP.getVersion());
         return true;
      } else {
         sender.sendMessage(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("commands-messages.admin.usage").replace("&", "§")));
         return true;
      }
   }
}
