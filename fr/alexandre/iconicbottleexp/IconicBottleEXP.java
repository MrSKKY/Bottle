package fr.alexandre.iconicbottleexp;

import commands.CommandsAdmin;
import commands.CommandsBottle;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class IconicBottleEXP extends JavaPlugin implements Listener {
   private String version = "v1.3";
   private static FileConfiguration langConfig;
   private Map<String, FileConfiguration> languageConfigs = new HashMap();

   public void onEnable() {
      this.setupConfig();
      this.getLogger().info("\u001b[36m----------------------------------");
      this.getLogger().info("\u001b[32m▸ IconicBottleEXP was successfully started!");
      this.getLogger().info("  \u001b[33m• Plugin by: Alexandr3_");
      this.getLogger().info("  \u001b[33m• Version: " + this.version);
      this.getLogger().info("\u001b[36m----------------------------------");
      this.getServer().getPluginManager().registerEvents(this, this);
      new CommandsAdmin(this);
      new CommandsBottle(this);
      this.getCommand("bottleadmin").setExecutor(new CommandsAdmin(this));
      this.getCommand("bottlexp").setExecutor(new CommandsBottle(this));
   }

   public void onDisable() {
      this.getLogger().info("\u001b[36m----------------------------------");
      this.getLogger().info("\u001b[31m▸ IconicBottleEXP was successfully stopped!");
      this.getLogger().info("  \u001b[33m• Plugin by: Alexandr3_");
      this.getLogger().info("  \u001b[33m• Version: " + this.version);
      this.getLogger().info("\u001b[36m----------------------------------");
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      ItemStack item = event.getItem();
      Material experienceBottleMaterial = this.getExperienceBottleMaterial();
      if (item != null && item.getType() == experienceBottleMaterial) {
         ItemMeta meta = item.getItemMeta();
         if (meta != null && meta.hasLore() && meta.getDisplayName().equalsIgnoreCase(this.getConfig().getString("options.bottle-title").replace("&", "§"))) {
            if (!player.hasPermission(this.getConfig().getString("permissions.bottle-use"))) {
               player.sendMessage(this.replacePrefixPlaceholder(this.getLanguageString("messages.no-access").replace("&", "§")));
               event.setCancelled(true);
               return;
            }

            List<String> lore = meta.getLore();
            if (lore.size() >= 2) {
               String xpString = ChatColor.stripColor((String)lore.get(1));
               StringBuilder xpAmountString = new StringBuilder();
               boolean previousCharDigit = false;
               char[] var10 = xpString.toCharArray();
               int var11 = var10.length;

               for(int var12 = 0; var12 < var11; ++var12) {
                  char c = var10[var12];
                  if (Character.isDigit(c)) {
                     xpAmountString.append(c);
                     previousCharDigit = true;
                  } else if (previousCharDigit) {
                     break;
                  }
               }

               if (xpAmountString.length() > 0) {
                  try {
                     int xpAmount = Integer.parseInt(xpAmountString.toString());
                     player.giveExpLevels(xpAmount);
                     ItemStack itemToRemove = item.clone();
                     itemToRemove.setAmount(1);
                     player.getInventory().removeItem(new ItemStack[]{itemToRemove});
                     event.setCancelled(true);
                     String msgExpRecover = this.replacePrefixPlaceholder(this.getLanguageString("commands-messages.bottlexp.expRecover").replace("&", "§"));
                     msgExpRecover = msgExpRecover.replace("{exp}", String.valueOf(xpAmount));
                     player.sendMessage(msgExpRecover);
                  } catch (NumberFormatException var14) {
                     player.sendMessage(this.replacePrefixPlaceholder(this.getLanguageString("commands-messages.bottlexp.no-valid").replace("&", "§")));
                  }
               } else {
                  player.sendMessage(this.replacePrefixPlaceholder(this.getLanguageString("commands-messages.bottlexp.no-valid").replace("&", "§")));
               }
            } else {
               player.sendMessage(this.replacePrefixPlaceholder(this.getLanguageString("commands-messages.bottlexp.no-valid").replace("&", "§")));
            }
         }
      }

   }

   private Material getExperienceBottleMaterial() {
      String itemName = this.getConfig().getString("options.item");
      Material bottleMaterial = Material.matchMaterial(itemName);
      if (bottleMaterial == null) {
         bottleMaterial = Material.EXPERIENCE_BOTTLE;
      }

      return bottleMaterial;
   }

   public String getVersion() {
      return this.version;
   }

   public void setupConfig() {
      this.saveDefaultConfig();
      this.reloadConfig();
      String locale = this.getConfig().getString("options.locale", "en");
      this.loadLanguageFile(locale);
      File langFolder = new File(this.getDataFolder(), "lang");
      if (!langFolder.exists()) {
         langFolder.mkdirs();
      }

      this.saveResourceIfNotExists("lang/lang-en.yml");
      this.saveResourceIfNotExists("lang/lang-fr.yml");
      this.saveResourceIfNotExists("lang/lang-es.yml");
      this.saveResourceIfNotExists("lang/lang-germ.yml");
   }

   private void saveResourceIfNotExists(String resourcePath) {
      File file = new File(this.getDataFolder(), resourcePath);
      if (!file.exists()) {
         this.saveResource(resourcePath, false);
      }

   }

   public void loadLanguageFile(String locale) {
      File langFolder = new File(this.getDataFolder(), "lang");
      if (!langFolder.exists()) {
         langFolder.mkdirs();
      }

      String langFileName = "lang-" + locale + ".yml";
      File langFile = new File(langFolder, langFileName);
      if (!langFile.exists()) {
         this.saveResource("lang/" + langFileName, false);
      }

      langConfig = YamlConfiguration.loadConfiguration(langFile);
      langConfig.setDefaults(this.getConfig());
   }

   public String getLanguageString(String key) {
      return langConfig != null && langConfig.contains(key) ? langConfig.getString(key) : "";
   }

   public String getLanguage() {
      return this.getConfig().getString("options.locale");
   }

   public String replacePrefixPlaceholder(String message) {
      String prefix = this.getConfig().getString("options.prefix").replace("&", "§");
      return message.replace("{prefix}", prefix);
   }
}
