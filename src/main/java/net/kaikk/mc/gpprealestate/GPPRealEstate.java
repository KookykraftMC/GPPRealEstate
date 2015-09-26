package net.kaikk.mc.gpprealestate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GPPRealEstate extends JavaPlugin {
	
    Logger log;
    DataStore dataStore;
    
    // Dependencies Variables
    public static boolean vaultPresent = false;
    public static Economy econ = null;
    public static Permission perms = null;
    
    public void onEnable(){
        
        this.log = getLogger();
        
        this.dataStore = new DataStore(this);
        loadConfig(false);
        
        new EventListener(this).registerEvents();

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
        
        
        
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	
    	if(command.getName().equalsIgnoreCase("gpre") && sender.hasPermission("gprealestate.admin")){
    		
    		if(args.length == 0){
    			sender.sendMessage(dataStore.chatPrefix + ChatColor.RED + "Unknown command function.");
    			return true;
    		}
    		else if(args.length == 1){
    			
    			if(args[0].equalsIgnoreCase("version")){
    				sender.sendMessage(dataStore.chatPrefix + ChatColor.GREEN + "You are running " + ChatColor.RED + dataStore.pdf.getName() + ChatColor.GREEN + " version " + ChatColor.RED + dataStore.pdf.getVersion());
    				return true;
    			}
    			else if(args[0].equalsIgnoreCase("reload")){
    				loadConfig(true); 
    				sender.sendMessage(dataStore.chatPrefix + ChatColor.GREEN + "The config file was succesfully reloaded.");
    				return true;
    			}
    			else {
    				sender.sendMessage(dataStore.chatPrefix + ChatColor.GREEN + "I don't know what to do with that.");
        			return true;
    			}
    			
    		}
    		
    	}
    	else {
    		sender.sendMessage(ChatColor.RED + "You do not have permissions to use this command.");
			return false;
    	}
    	
    	return false;
    	
    }
    
    private void loadConfig(boolean reload){
    	dataStore.messages=new HashMap<String,String>();
		this.saveDefaultConfig();
		this.getConfig().options().copyDefaults(true);
        
    	// Loading the config file items that exsists or setting the default values.
        dataStore.cfgSignShort = this.getConfig().getString("GPRealEstate.Keywords.Signs.Short", "[RE]");
        dataStore.cfgSignLong = this.getConfig().getString("GPRealEstate.Keywords.Signs.Long", "[RealEstate]");
        
        dataStore.cfgRentKeywords = dataStore.stringToList(this.getConfig().getString("GPRealEstate.Keywords.Actions.Renting", "Rent;Renting;Rental;For Rent"));
        dataStore.cfgSellKeywords = dataStore.stringToList(this.getConfig().getString("GPRealEstate.Keywords.Actions.Selling", "Sell;Selling;For Sale"));
        
        dataStore.cfgReplaceRent = this.getConfig().getString("GPRealEstate.Keywords.Actions.ReplaceRent", "FOR LEASE");
        dataStore.cfgReplaceSell = this.getConfig().getString("GPRealEstate.Keywords.Actions.ReplaceSell", "FOR SALE");
        
        dataStore.cfgEnableLeasing = this.getConfig().getBoolean("GPRealEstate.Rules.EnableLeasing", false);
        dataStore.cfgIgnoreClaimSize = this.getConfig().getBoolean("GPRealEstate.Rules.IgnoreSizeLimit", false);
        
        dataStore.dateFormat = this.getConfig().getString("GPRealEstate.DateFormat", "yyyy/MM/dd HH:mm:ss");
        
        if(!reload) {
        	// Letting the console know the "Keywords"
        	this.log.info("Signs will be using the keywords \"" + dataStore.cfgSignShort + "\" or \"" + dataStore.cfgSignLong + "\"");
        }
        ConfigurationSection messagesSection = this.getConfig().getConfigurationSection("GPRealEstate.Messages");
        for (String key : messagesSection.getKeys(false)) {
        	dataStore.messages.put(key, messagesSection.getString(key));
        }
        
        // Saving the config informations into the file.
        this.getConfig().set("GPRealEstate.Keywords.Signs.Short", dataStore.cfgSignShort);
        this.getConfig().set("GPRealEstate.Keywords.Signs.Long", dataStore.cfgSignLong);
        this.getConfig().set("GPRealEstate.Keywords.Actions.Renting", dataStore.listToString(dataStore.cfgRentKeywords));
        this.getConfig().set("GPRealEstate.Keywords.Actions.Selling", dataStore.listToString(dataStore.cfgSellKeywords));
        this.getConfig().set("GPRealEstate.Keywords.Actions.ReplaceRent", dataStore.cfgReplaceRent);
        this.getConfig().set("GPRealEstate.Keywords.Actions.ReplaceSell", dataStore.cfgReplaceSell);
        this.getConfig().set("GPRealEstate.Rules.EnableLeasing", dataStore.cfgEnableLeasing);
        this.getConfig().set("GPRealEstate.Rules.IgnoreSizeLimit", dataStore.cfgIgnoreClaimSize);
        
        try {
        	this.getConfig().save(dataStore.configFilePath);
        } catch(IOException exception){
        	this.log.info("Unable to write to the configuration file at \"" + dataStore.configFilePath + "\"");
        }
        
    }

    public void addLogEntry(String entry) {
        try {
            File logFile = new File(dataStore.logFilePath);
            
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
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = (Economy)rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions(){
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = (Permission)rsp.getProvider();
        return perms != null;
    }
}