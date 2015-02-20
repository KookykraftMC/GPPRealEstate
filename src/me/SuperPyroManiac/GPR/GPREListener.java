package me.SuperPyroManiac.GPR;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;

public class GPREListener implements Listener {
    
    private GPRealEstate plugin;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    Date date = new Date();

    public GPREListener(GPRealEstate plugin){
        this.plugin = plugin;
    }

    public void registerEvents(){
        PluginManager pm = this.plugin.getServer().getPluginManager();
        pm.registerEvents(this, this.plugin);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event){
        
        if ((event.getLine(0).equalsIgnoreCase(DataStore.cfgShortSignName)) || (event.getLine(0).equalsIgnoreCase(DataStore.cfgLongSignName))) {
            
            Player signPlayer = event.getPlayer();
            Location signLocation = event.getBlock().getLocation();

            GriefPrevention gp = GriefPrevention.instance;

            Claim signClaim = gp.dataStore.getClaimAt(signLocation, false, null);

            if (signClaim == null) {
                signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "The sign you placed is not inside a claim!");
                event.setCancelled(true);
                return;
            }

            if (!GPRealEstate.perms.has(signPlayer, "gprealestate.sell")) {
                signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You do not have permission to sell claims!");
                event.setCancelled(true);
                return;
            }

            if (event.getLine(1).isEmpty()) {
                signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You need to enter the price on the second line!");
                event.setCancelled(true);
                return;
            }

            String signCost = event.getLine(1);

            try {
                double d = Double.parseDouble(event.getLine(1));
            } 
            catch (NumberFormatException e) {
                signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You need to enter a valid number on the second line.");
                event.setCancelled(true);
                return;
            }

            if (signClaim.parent == null){
                
                if (signPlayer.getName().equalsIgnoreCase(signClaim.getOwnerName())) {
                    
                    event.setLine(0, DataStore.cfgLongSignName);
                    event.setLine(1, ChatColor.GREEN + "FOR SALE");
                    event.setLine(2, signPlayer.getName());
                    event.setLine(3, signCost + " " + GPRealEstate.econ.currencyNamePlural());
                    
                    signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.AQUA + "This claim is now for sale, for " + ChatColor.GREEN + signCost + " " + GPRealEstate.econ.currencyNamePlural());
                    
                    GPRealEstate.addLogEntry("[" + this.dateFormat.format(this.date) + "] " + signPlayer.getName() + " has made a claim for sale at [" + signPlayer.getLocation().getWorld() + ", X: " + 
                    signPlayer.getLocation().getBlockX() + ", Y: " + signPlayer.getLocation().getBlockY() + ", Z: " + signPlayer.getLocation().getBlockZ() + "] Price: " + signCost + " " + GPRealEstate.econ.currencyNamePlural());
                    
                } else {
                    
                    if (signClaim.isAdminClaim()){
                        
                        if (signPlayer.hasPermission("gprealestate.adminclaim")) {
                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You cannot sell admin claims, they can only be leased!");
                            event.setCancelled(true);
                            return;
                        }
                        
                    }

                    signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You can only sell claims you own!");
                    event.setCancelled(true);
                    
                }

            }
            else if ((signPlayer.getName().equalsIgnoreCase(signClaim.parent.getOwnerName())) || (signClaim.managers.equals(signPlayer.getName()))) {
                
                if (signPlayer.hasPermission("gprealestate.sellsub")){
                    
                    event.setLine(0, DataStore.cfgLongSignName);
                    event.setLine(1, ChatColor.GREEN + "FOR LEASE");
                    event.setLine(2, signPlayer.getName());
                    event.setLine(3, signCost + " " + GPRealEstate.econ.currencyNamePlural());
                    
                    signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.AQUA + "This subclaim is now for lease, for " + ChatColor.GREEN + signCost + " " + GPRealEstate.econ.currencyNamePlural());

                    GPRealEstate.addLogEntry("[" + this.dateFormat.format(this.date) + "] " + signPlayer.getName() + " has made a subclaim for lease at [" + signPlayer.getLocation().getWorld() + ", X: " + 
                    signPlayer.getLocation().getBlockX() + ", Y: " + signPlayer.getLocation().getBlockY() + ", Z: " + signPlayer.getLocation().getBlockZ() + "] Price: " + signCost + " " + GPRealEstate.econ.currencyNamePlural());
                    
                } else {
                    signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You do not have permission to sell subclaims!");
                }
                
            } else if ((signClaim.parent.isAdminClaim()) && (signPlayer.hasPermission("gprealestate.adminclaim"))) {
                
                event.setLine(0, DataStore.cfgLongSignName);
                event.setLine(1, ChatColor.GREEN + "FOR LEASE");
                event.setLine(2, "Server");
                event.setLine(3, signCost + " " + GPRealEstate.econ.currencyNamePlural());
                
                signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.AQUA + "This admin subclaim is now for lease, for " + ChatColor.GREEN + signCost + GPRealEstate.econ.currencyNamePlural());
                
                GPRealEstate.addLogEntry("[" + this.dateFormat.format(this.date) + "] " + signPlayer.getName() + " Has made an admin subclaim for lease at [" + signPlayer.getLocation().getWorld() + ", X: " + 
                signPlayer.getLocation().getBlockX() + ", Y: " + signPlayer.getLocation().getBlockY() + ", Z: " + signPlayer.getLocation().getBlockZ() + "] Price: " + signCost + " " + GPRealEstate.econ.currencyNamePlural());
    
            } else {
                signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You can only lease subclaims you own!");
                event.setCancelled(true);
                return;
            }
            
        }
        
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
    
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            
            Material type = event.getClickedBlock().getType();
            
            if ((type == Material.SIGN_POST) || (type == Material.WALL_SIGN)) {
                
                Sign sign = (Sign)event.getClickedBlock().getState();

                if ((sign.getLine(0).equalsIgnoreCase(DataStore.cfgShortSignName)) || (sign.getLine(0).equalsIgnoreCase(DataStore.cfgLongSignName))) {
                    
                    Player signPlayer = event.getPlayer();
                    
                    if (!GPRealEstate.perms.has(signPlayer, "gprealestate.buy")) {
                        signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You do not have permission to buy claims!");
                        event.setCancelled(true);
                        return;
                    }

                    Location signLocation = event.getClickedBlock().getLocation();
                    GriefPrevention gp = GriefPrevention.instance;
                    Claim signClaim = gp.dataStore.getClaimAt(signLocation, false, null);

                    if (signClaim == null) {
                        signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "This sign is no longer within a claim!");
                        return;
                    }

                    if (signClaim.parent == null){
                        
                        if ((!sign.getLine(2).equalsIgnoreCase(signClaim.getOwnerName())) && (!signClaim.isAdminClaim())) {
                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "The listed player no longer has the rights to sell this claim!");
                            event.getClickedBlock().setType(Material.AIR);
                            return;
                        }

                        if (signClaim.getOwnerName().equalsIgnoreCase(signPlayer.getName())) {
                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.YELLOW + "You already own this claim!");
                            return;
                        }
                        
                    }
                    else {
                        
                        if ((!sign.getLine(2).equalsIgnoreCase(signClaim.parent.getOwnerName())) && (!signClaim.managers.equals(sign.getLine(2))) && (!signClaim.parent.isAdminClaim())) {
                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "The listed player no longer has the rights to lease this claim!");
                            event.getClickedBlock().setType(Material.AIR);
                            return;
                        }

                        if (signClaim.parent.getOwnerName().equalsIgnoreCase(signPlayer.getName())) {
                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.YELLOW + "You already own this claim!");
                            return;
                        }

                        if ((sign.getLine(1).equalsIgnoreCase(ChatColor.GREEN + "FOR SALE")) || (sign.getLine(1).equalsIgnoreCase("FOR SALE"))) {
                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "Misplaced sign!");
                            event.getClickedBlock().setType(Material.AIR);
                            return;
                        }
                        
                    }

                    String[] signDelimit = sign.getLine(3).split(" ");
                    Double signCost = Double.valueOf(Double.valueOf(signDelimit[0].trim()).doubleValue());

                    if (!GPRealEstate.econ.has(signPlayer.getName(), signCost.doubleValue())) {
                        signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You do not have enough money!");
                        return;
                    }

                    EconomyResponse ecoresp = GPRealEstate.econ.withdrawPlayer(signPlayer.getName(), signCost.doubleValue());
                    
                    if (!ecoresp.transactionSuccess()) {
                        signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "Could not withdraw the money!");
                        return;
                    }

                    if (!sign.getLine(2).equalsIgnoreCase("server")) {
                        
                        ecoresp = GPRealEstate.econ.depositPlayer(sign.getLine(2), signCost.doubleValue());
                        
                        if (!ecoresp.transactionSuccess()) {
                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "Could not transfer money, refunding Player!");
                            GPRealEstate.econ.depositPlayer(signPlayer.getName(), signCost.doubleValue());
                            return;
                        }

                    }

                    if ((sign.getLine(1).equalsIgnoreCase(ChatColor.GREEN + "FOR SALE")) || (sign.getLine(1).equalsIgnoreCase("FOR SALE"))) {
                        
                    	if(DataStore.cfgIgnoreClaimSize || (signClaim.getArea() < gp.dataStore.getPlayerData(signPlayer.getUniqueId()).getAccruedClaimBlocks()) || signPlayer.hasPermission("gprealestate.ignoresizelimit")){
                    	
	                        try {
	                        	
	                            for (Claim child : signClaim.children) {
	                                child.clearPermissions();
	                                child.managers.remove(child.getOwnerName());
	                            }
	
	                            signClaim.clearPermissions();
	                            gp.dataStore.changeClaimOwner(signClaim,signPlayer.getUniqueId());
	                            
	                        } 
	                        catch (Exception e) {
	                            e.printStackTrace();
	                            return;
	                        }

	                        if (signClaim.getOwnerName().equalsIgnoreCase(signPlayer.getName())) {
	                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.AQUA + "You have successfully purchased this claim for " + ChatColor.GREEN + signCost + GPRealEstate.econ.currencyNamePlural());
	                            GPRealEstate.addLogEntry("[" + this.dateFormat.format(this.date) + "] " + signPlayer.getName() + " Has purchased a claim at [" + signPlayer.getLocation().getWorld() + ", X: " + 
	                            signPlayer.getLocation().getBlockX() + ", Y: " + signPlayer.getLocation().getBlockY() +", Z: " + signPlayer.getLocation().getBlockZ() + "] Price: " + signCost + " " + GPRealEstate.econ.currencyNamePlural());
	                        } else {
	                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "Cannot purchase claim!");
	                            return;
	                        }
	                        
	                        gp.dataStore.saveClaim(signClaim);
	                        event.getClickedBlock().breakNaturally();
                        
                    	}
                    	else {
                    		signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You do not have enough claim blocks available.");
                    		return;
                    	}
                        
                    }

                    if ((sign.getLine(1).equalsIgnoreCase(ChatColor.GREEN + "FOR LEASE")) || (sign.getLine(1).equalsIgnoreCase("FOR LEASE"))){
                        
                        if (GPRealEstate.perms.has(signPlayer, "gprealestate.buysub")) {
                            
                            signClaim.clearPermissions();
                            
                            if (!sign.getLine(2).equalsIgnoreCase("server")) {
                                signClaim.managers.remove(sign.getLine(2));
                            }

                            signClaim.managers.add(signPlayer.getUniqueId().toString());							// Allowing the player to manage permissions.
                            signClaim.setPermission(signPlayer.getUniqueId().toString(), ClaimPermission.Build);	// Allowing the player to build in the subclaim!
                            gp.dataStore.saveClaim(signClaim);
                            event.getClickedBlock().breakNaturally();
                            
                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.AQUA + "You have successfully purchased this subclaim for " + ChatColor.GREEN + signCost + GPRealEstate.econ.currencyNamePlural());

                            GPRealEstate.addLogEntry("[" + this.dateFormat.format(this.date) + "] " + signPlayer.getName() + " Has purchased a subclaim at [" + signPlayer.getLocation().getWorld() + ", X: " + 
                            signPlayer.getLocation().getBlockX() + ", Y: " + signPlayer.getLocation().getBlockY() + ", Z: " + signPlayer.getLocation().getBlockZ() + "] Price: " + signCost + " " + GPRealEstate.econ.currencyNamePlural());
                        
                        } else {
                            signPlayer.sendMessage(DataStore.chatPrefix + ChatColor.RED + "You do not have permission to buy subclaims!");
                        }
                        
                    }

                }

            }

        }

    }
    
}