package me.SuperPyroManiac.GPR;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;

public class DataStore {
	
	GPRealEstate plugin;
	
	// Plugin Description File (plugin.yml) access.
    public PluginDescriptionFile pdf;

	// Plugin File Paths
	public final String pluginDirPath = "plugins" + File.separator + "GPRealEstate" + File.separator;
	public final String configFilePath = pluginDirPath + "config.yml";
    public final String logFilePath = pluginDirPath + "GPRealEstate.log";
    
    // Plugin Log/Chat Prefix
    public final String chatPrefix = "[" + ChatColor.GOLD + "GPRealEstate" + ChatColor.WHITE + "] ";
    
    // Config Variables
    public String cfgSignShort;
    public String cfgSignLong;
    
    public List<String> cfgRentKeywords;
    public List<String> cfgSellKeywords;
    
    public String cfgReplaceRent;
    public String cfgReplaceSell;
    
    public boolean cfgIgnoreClaimSize;
    
    public DataStore(GPRealEstate plugin){
    	this.plugin = plugin;
    	this.pdf = this.plugin.getDescription();
    }
    
    public List<String> stringToList(String input){
    	String[] array = input.matches("([;+])") ? input.split(";") : new String[]{input};
    	return Arrays.asList(array);
    }
    
    public String listToString(List<String> input){
    	String string = "";
    	int count = 1;
    	for(Object str : input.toArray()){
    		if(count != 1) {
    			count++;
    			string += ";";
    		}
    		string += str.toString();
    	}
    	return string;
    }
    
}