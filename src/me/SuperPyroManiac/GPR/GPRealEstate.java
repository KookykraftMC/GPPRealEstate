package me.SuperPyroManiac.GPR;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GPRealEstate extends JavaPlugin {
	
    Logger log;
    
    PluginDescriptionFile pdf;
    
    // Dependencies Variables
    public static boolean vaultPresent = false;
    public static Economy econ = null;
    public static Permission perms = null;
    
    public void onEnable(){
        
        this.log = getLogger();
        this.pdf = this.getDescription();
        
        new GPREListener(this).registerEvents();

        if (checkVault()) {
            
            this.log.info("Vault has been detected and enabled.");
            
            if (setupEconomy()) {
                this.log.info("Vault is using " + econ.getName() + " as the economy plugin.");
            } else {
                this.log.warning("No compatible economy plugin detected [Vault].");
                this.log.warning("Disabling plugin.");
                getPluginLoader().disablePlugin(this);
                return;
            }
            
            if (setupPermissions()) {
                this.log.info("Vault is using " + perms.getName() + " for the permissions.");
            } else {
                this.log.warning("No compatible permissions plugin detected [Vault].");
                this.log.warning("Disabling plugin.");
                getPluginLoader().disablePlugin(this);
                return;
            }
            
        }

        loadConfig(false);
        
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	
    	if(command.getName().equalsIgnoreCase("gpre")){
    		
    		if(args.length == 0){
    			sender.sendMessage(DataStore.chatPrefix + ChatColor.RED + "Unknown command function.");
    			return true;
    		}
    		else if(args.length == 1){
    			
    			if(args[0].equalsIgnoreCase("version")){
    				sender.sendMessage(DataStore.chatPrefix + ChatColor.GREEN + "You are running " + ChatColor.RED + pdf.getName() + ChatColor.GREEN + " version " + ChatColor.RED + pdf.getVersion());
    				return true;
    			}
    			else if(args[0].equalsIgnoreCase("reload")){
    				loadConfig(true); 
    				sender.sendMessage(DataStore.chatPrefix + ChatColor.GREEN + "The config file was succesfully reloaded.");
    				return true;
    			}
    			else {
    				sender.sendMessage(DataStore.chatPrefix + ChatColor.GREEN + "I don't know what to do with that.");
        			return true;
    			}
    			
    		}
    		
    	}
    	
    	return false;
    	
    }
    
    private void loadConfig(boolean reload){
    	
    	FileConfiguration config = YamlConfiguration.loadConfiguration(new File(DataStore.configFilePath));
        FileConfiguration outConfig = new YamlConfiguration();
        
    	// Loading the config file items that exsists.
        DataStore.cfgShortSignName = config.getString("GPRealEstate.Keywords.Short", "[RE]");
        DataStore.cfgLongSignName = config.getString("GPRealEstate.Keywords.Long", "[RealEstate]");
        DataStore.cfgIgnoreClaimSize = config.getBoolean("GPRealEstate.Rules.IgnoreSizeLimit", false);
        
        if(!reload) {
        	// Letting the console know the "Keywords"
        	this.log.info("Signs will be using the keywords \"" + DataStore.cfgShortSignName + "\" or \"" + DataStore.cfgLongSignName + "\"");
        }
        
        // Saving the confige informations down.
        outConfig.set("GPRealEstate.Keywords.Short", DataStore.cfgShortSignName);
        outConfig.set("GPRealEstate.Keywords.Long", DataStore.cfgLongSignName);
        outConfig.set("GPRealEstate.Rules.IgnoreSizeLimit", DataStore.cfgIgnoreClaimSize);
        
        try {
        	outConfig.save(DataStore.configFilePath);
        }
        catch(IOException exception){
        	this.log.info("Unable to write to the configuration file at \"" + DataStore.configFilePath + "\"");
        }
        
    }

    public static void addLogEntry(String entry) {
        try {
            File logFile = new File(DataStore.logFilePath);
            
            if (!logFile.exists()) { 
            	logFile.createNewFile(); 
            }
            
            FileWriter fw = new FileWriter(logFile, true);
            PrintWriter pw = new PrintWriter(fw);
            
            pw.println(entry);
            pw.flush();
            pw.close();
        } 
        catch (IOException e) { e.printStackTrace(); }
    }

    private boolean checkVault(){
        vaultPresent = getServer().getPluginManager().getPlugin("Vault") != null;
        return vaultPresent;
    }

    private boolean setupEconomy(){
        RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = (Economy)rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions(){
        RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = (Permission)rsp.getProvider();
        return perms != null;
    }
}