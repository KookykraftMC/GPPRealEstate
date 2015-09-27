package net.kaikk.mc.gpprealestate;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;

public class DataStore {
	GPPRealEstate plugin;

    
    // Plugin Log/Chat Prefix
    public final String chatPrefix = "[" + ChatColor.GOLD + "GPPRealEstate" + ChatColor.WHITE + "] ";
    
    // Config Variables
    public String cfgSignShort;
    public String cfgSignLong;
    
    public List<String> cfgRentKeywords;
    public List<String> cfgSellKeywords;
    
    public String cfgReplaceRent;
    public String cfgReplaceSell;
    
    public boolean cfgEnableLeasing;
    public boolean cfgIgnoreClaimSize;
    
    public String dateFormat; 
    
    Map<String, String> messages;
    
    public DataStore(GPPRealEstate plugin){
    	this.plugin = plugin;
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