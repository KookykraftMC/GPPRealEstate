package me.SuperPyroManiac.GPR;

import java.io.File;
import org.bukkit.ChatColor;

public class DataStore {

	// Plugin File Paths
	public static final String pluginDirPath = "plugins" + File.separator + "GPRealEstate" + File.separator;
	public static final String configFilePath = pluginDirPath + "config.yml";
    public static final String logFilePath = pluginDirPath + "GPRealEstate.log";
    
    // Plugin Log/Chat Prefix
    public static final String chatPrefix = "[" + ChatColor.GOLD + "GPRealEstate" + ChatColor.WHITE + "] ";
    
    // Config Setups
    public static String cfgShortSignName;
    public static String cfgLongSignName;
    public static boolean cfgIgnoreClaimSize;
    
}