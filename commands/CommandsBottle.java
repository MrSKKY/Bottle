package commands;

import fr.alexandre.iconicbottleexp.IconicBottleEXP;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandsBottle implements CommandExecutor {
   private IconicBottleEXP iconicBottleEXP;

   public CommandsBottle(IconicBottleEXP iconicBottleEXP) {
      this.iconicBottleEXP = iconicBottleEXP;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getConfig().getString("messages.notplayer").replace("&", "§")));
         return true;
      } else {
         Player player = (Player)sender;
         if (label.equalsIgnoreCase("bottlexp")) {
            if (player.hasPermission(this.iconicBottleEXP.getConfig().getString("permissions.player-use"))) {
               if (args.length == 1) {
                  try {
                     int expAmount = Integer.parseInt(args[0]);
                     int minAmount = this.iconicBottleEXP.getConfig().getInt("options.expAmount-min");
                     int maxAmount = this.iconicBottleEXP.getConfig().getInt("options.expAmount-max");
                     if (expAmount < minAmount || expAmount > maxAmount) {
                        String message = this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("commands-messages.bottlexp.expMinMax"));
                        message = message.replace("&", "§");
                        message = message.replace("{expMin}", String.valueOf(minAmount));
                        message = message.replace("{expMax}", String.valueOf(maxAmount));
                        player.sendMessage(message);
                        return true;
                     }

                     if (player.getLevel() < expAmount) {
                        player.sendMessage(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("commands-messages.bottlexp.not-enough-exp").replace("&", "§")));
                        return true;
                     }

                     if (player.getInventory().firstEmpty() == -1) {
                        if (this.hasExperienceBottle(player.getInventory(), expAmount)) {
                           this.convertToBottle(player, expAmount);
                        } else {
                           player.sendMessage(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("commands-messages.bottlexp.no-inventory-space").replace("&", "§")));
                        }

                        return true;
                     }

                     this.convertToBottle(player, expAmount);
                  } catch (NumberFormatException var10) {
                     player.sendMessage(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("commands-messages.bottlexp.not-number").replace("&", "§")));
                  }
               } else {
                  player.sendMessage(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("commands-messages.bottlexp.usage").replace("&", "§")));
               }
            } else {
               player.sendMessage(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("messages.no-permission").replace("&", "§")));
            }
         }

         return true;
      }
   }

   boolean hasExperienceBottle(Inventory inventory, int expAmount) {
      String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
      Material experienceBottleMaterial = this.getExperienceBottleMaterial();
      ItemStack[] var5 = inventory.getContents();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         ItemStack item = var5[var7];
         if (item != null && item.getType() == experienceBottleMaterial && this.getExperienceFromBottle(item) == expAmount) {
            return true;
         }
      }

      return false;
   }

   private int getExperienceFromBottle(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null && meta.hasLore()) {
         List<String> lore = meta.getLore();
         Iterator var4 = lore.iterator();

         while(true) {
            StringBuilder xpAmountString;
            do {
               if (!var4.hasNext()) {
                  return 0;
               }

               String line = (String)var4.next();
               String xpString = ChatColor.stripColor(line);
               xpAmountString = new StringBuilder();
               char[] var8 = xpString.toCharArray();
               int var9 = var8.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  char c = var8[var10];
                  if (Character.isDigit(c)) {
                     xpAmountString.append(c);
                  }
               }
            } while(xpAmountString.length() <= 0);

            try {
               return Integer.parseInt(xpAmountString.toString());
            } catch (NumberFormatException var12) {
            }
         }
      } else {
         return 0;
      }
   }

   private Material getExperienceBottleMaterial() {
      String itemName = this.iconicBottleEXP.getConfig().getString("options.item");
      Material bottleMaterial = Material.matchMaterial(itemName);
      if (bottleMaterial == null) {
         bottleMaterial = Material.EXPERIENCE_BOTTLE;
      }

      return bottleMaterial;
   }

   void convertToBottle(Player player, int expAmount) {
      if (this.hasExperienceBottle(player.getInventory(), expAmount)) {
         String messageConvertFailed = this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("commands-messages.bottlexp.alreadyExists"));
         messageConvertFailed = messageConvertFailed.replace("&", "§");
         player.sendMessage(messageConvertFailed);
      } else {
         ItemStack xpBottle = this.createBottledExp(expAmount, player.getName());
         player.getInventory().addItem(new ItemStack[]{xpBottle});
         player.setLevel(player.getLevel() - expAmount);
         String messageConvert = this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getLanguageString("commands-messages.bottlexp.bottle-converted"));
         messageConvert = messageConvert.replace("&", "§");
         messageConvert = messageConvert.replace("{exp}", String.valueOf(expAmount));
         player.sendMessage(messageConvert);
      }
   }

   public ItemStack createBottledExp(int xpAmount, String playerName) {
      Material bottleMaterial = this.getExperienceBottleMaterial();
      ItemStack bottle = new ItemStack(bottleMaterial);
      ItemMeta meta = bottle.getItemMeta();
      meta.setDisplayName(this.iconicBottleEXP.replacePrefixPlaceholder(this.iconicBottleEXP.getConfig().getString("options.bottle-title").replace("&", "§")));
      List<String> lore = this.iconicBottleEXP.getConfig().getStringList("options.bottle-lore");
      List<String> formattedLore = new ArrayList();
      Iterator var8 = lore.iterator();

      while(var8.hasNext()) {
         String line = (String)var8.next();
         line = this.iconicBottleEXP.replacePrefixPlaceholder(line.replace("&", "§"));
         line = line.replace("{xp}", String.valueOf(xpAmount));
         line = line.replace("{player}", playerName);
         formattedLore.add(line);
      }

      meta.setLore(formattedLore);
      bottle.setItemMeta(meta);
      return bottle;
   }
}
