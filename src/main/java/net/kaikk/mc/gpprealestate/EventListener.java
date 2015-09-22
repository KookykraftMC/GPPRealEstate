package net.kaikk.mc.gpprealestate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.ClaimPermission;
import net.kaikk.mc.gpp.GriefPreventionPlus;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;

public class EventListener implements Listener {
    
    private GPPRealEstate plugin;
    
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();

    public EventListener(GPPRealEstate plugin){
        this.plugin = plugin;
    }

    public void registerEvents(){
        PluginManager pm = this.plugin.getServer().getPluginManager();
        pm.registerEvents(this, this.plugin);
    }
    
    private boolean makePayment(Player sender, OfflinePlayer receiver, Double price){
    	if (!GPPRealEstate.econ.has(sender, price.doubleValue())) {
    		sender.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You do not have enough money!");
            return false;
        }

        EconomyResponse ecoresp = GPPRealEstate.econ.withdrawPlayer(sender, price.doubleValue());

        if (!ecoresp.transactionSuccess()) {
        	sender.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "Could not withdraw the money!");
            return false;
        }

        if (receiver!=null) {
            ecoresp = GPPRealEstate.econ.depositPlayer(receiver, price.doubleValue());

            if (!ecoresp.transactionSuccess()) {
            	sender.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "Could not transfer money, refunding Player!");
                GPPRealEstate.econ.depositPlayer(sender, price.doubleValue());
                return false;
            }
        }
        
        return true;
    }

    @EventHandler 	// Player creates a sign
    public void onSignChange(SignChangeEvent event){
    	
        // When a sign is being created..
    	if((event.getLine(0).equalsIgnoreCase(plugin.dataStore.cfgSignShort)) || (event.getLine(0).equalsIgnoreCase(plugin.dataStore.cfgSignLong))){
    		
    		Player player = event.getPlayer();									// The Player
            Location location = event.getBlock().getLocation();					// The Sign Location

            GriefPreventionPlus gpp = GriefPreventionPlus.getInstance();						// The GriefPreventionPlus Instance
            Claim claim = gpp.getDataStore().getClaimAt(location, false, null);		// The Claim which contains the Sign.

            if (claim == null) {
            	// The sign is not inside a claim.
            	player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "The sign you placed is not inside a claim!");
                event.setCancelled(true);
                return;
            }
            
            if (event.getLine(1).isEmpty()) {
            	// The player did NOT enter a price on the second line.
            	player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You need to enter the price on the second line!");
                event.setCancelled(true);
                return;
            }
            
            String price = event.getLine(1);
            
            try {
                Double.parseDouble(event.getLine(1));
            }
            catch (NumberFormatException e) {
            	// Invalid input on second line, it has to be a NUMBER!
                player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "The price you entered is not a valid number!");
                event.setCancelled(true);
                return;
            }
            
            if(claim.getParent() == null){
            	// This is a "claim"
                
                if(player.getUniqueId().equals(claim.getOwnerID())){
                	if (!GPPRealEstate.perms.has(player, "GPRealEstate.claim.sell")) {
                    	// The player does NOT have the correct permissions to sell claims
                    	player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You do not have permission to sell claims!");
                        event.setCancelled(true);
                        return;
                    }
                	
                	// Putting the claim up for sale!
                	event.setLine(0, plugin.dataStore.cfgSignLong);
                    event.setLine(1, ChatColor.DARK_GREEN + plugin.dataStore.cfgReplaceSell);
                    event.setLine(2, player.getName());
                    event.setLine(3, price + " " + GPPRealEstate.econ.currencyNamePlural());

                    player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.AQUA + "You are now selling this claim for " + ChatColor.GREEN + price + " " + GPPRealEstate.econ.currencyNamePlural());

                    plugin.addLogEntry(
                    	"[" + this.dateFormat.format(this.date) + "] " + player.getName() + " has made a claim for sale at [" 
                    	+ player.getLocation().getWorld() + ", "
                    	+ "X: " + player.getLocation().getBlockX() + ", "
                    	+ "Y: " + player.getLocation().getBlockY() + ", "
                    	+ "Z: " + player.getLocation().getBlockZ() + "] "
                    	+ "Price: " + price + " " + GPPRealEstate.econ.currencyNamePlural()
                    );
            	
                }
                else {
                	
                	if (claim.isAdminClaim()){
                		// This is a "Admin Claim" they cannot be sold!
                        if (player.hasPermission("GPRealEstate.admin")) {
                            player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You cannot sell admin claims, they can only be leased!");
                            event.setCancelled(true);
                            return;
                        }
                    }

                	player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You can only sell claims you own!");
                    event.setCancelled(true);
                    return;
                    
                }
                
            }
            else if (player.getUniqueId().equals(claim.getParent().getOwnerID()) || claim.hasExplicitPermission(player, ClaimPermission.MANAGE)) {
            	// This is a "subclaim"
            	
            	if (GPPRealEstate.perms.has(player, "GPRealEstate.subclaim.sell")) {
            		
            		String period = event.getLine(2);
            		
                	if(period.isEmpty()){
                		
                		// One time Leasing, player pays once for renting a claim.
                		event.setLine(0, plugin.dataStore.cfgSignLong);
                        event.setLine(1, ChatColor.DARK_GREEN + plugin.dataStore.cfgReplaceSell);
                        event.setLine(2, player.getName());
                        event.setLine(3, price + " " + GPPRealEstate.econ.currencyNamePlural());
                        
                        player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.AQUA + "You are now selling access to this subclaim for " + ChatColor.GREEN + price + " " + GPPRealEstate.econ.currencyNamePlural());

                        plugin.addLogEntry(
                    		"[" + this.dateFormat.format(this.date) + "] " + player.getName() + " has made a subclaim access for sale at "
                    		+ "[" + player.getLocation().getWorld() + ", "
                    		+ "X: " + player.getLocation().getBlockX() + ", "
                    		+ "Y: " + player.getLocation().getBlockY() + ", "
                    		+ "Z: " + player.getLocation().getBlockZ() + "] "
                    		+ "Price: " + price + " " + GPPRealEstate.econ.currencyNamePlural()
                        );
                        
                	}
                	else {
                		
                		// Leasing with due time, player pays once every "X" for a subclaim.
                		if(plugin.dataStore.cfgEnableLeasing){
                			
                			/*if(2 > 10){ // FIXME dead code, wtf :\
                				if(!period.matches("^([0-9]{1,3})(w|d|h){1}$")){
    	                			player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "The leasing period you wrote is not correct.");
    	                			event.getBlock().breakNaturally();
    	                            event.setCancelled(true);
    	                            return;
    	                		}
    	                		
    	                		event.setLine(0, plugin.dataStore.cfgSignLong);
    	                        event.setLine(1, ChatColor.DARK_GREEN + plugin.dataStore.cfgReplaceRent);
    	                        event.setLine(2, player.getName());
    	                        event.setLine(3, price + " " + GPPRealEstate.econ.currencyNamePlural());
    	                		
    	                        player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.AQUA + "You are now leasing this subclaim for " + ChatColor.GREEN + price + " " + GPPRealEstate.econ.currencyNamePlural());
    	
    	                        plugin.addLogEntry(
    	                    		"[" + this.dateFormat.format(this.date) + "] " + player.getName() + " has made a subclaim for lease at "
    	                    		+ "[" + player.getLocation().getWorld() + ", "
    	                    		+ "X: " + player.getLocation().getBlockX() + ", "
    	                    		+ "Y: " + player.getLocation().getBlockY() + ", "
    	                    		+ "Z: " + player.getLocation().getBlockZ() + "] "
    	                    		+ "Price: " + price + " " + GPPRealEstate.econ.currencyNamePlural()
    	                        );
                			}
                			else {*/
                				player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.DARK_PURPLE + "This feature is not yet fully implemented!");
                    			event.getBlock().breakNaturally();
                    			return;
                			//}
                		
                		}
                		else {
                			player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.DARK_PURPLE + "Leasing has been disabled!");
                			event.getBlock().breakNaturally();
                			return;
                		}
                        
                	}
                	
                }
            	else {
            		// The player does NOT have the correct permissions to sell subclaims
                	player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You do not have permission to sell subclaims!");
                    event.setCancelled(true);
                    return;
            	}
            	
            } // Second IF
            
    	} // First IF
    	
    }

    @EventHandler 	// Player interacts with a block.
    public void onSignInteract(PlayerInteractEvent event) {
    	
    	if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
    		
    		Material type = event.getClickedBlock().getType();
            if ((type == Material.SIGN_POST) || (type == Material.WALL_SIGN)) {
            	
            	Sign sign = (Sign)event.getClickedBlock().getState();
                if ((sign.getLine(0).equalsIgnoreCase(plugin.dataStore.cfgSignShort)) || (sign.getLine(0).equalsIgnoreCase(plugin.dataStore.cfgSignLong))) {
                	
                	Player player = event.getPlayer();
                	
                    Location location = event.getClickedBlock().getLocation();
                    
                    GriefPreventionPlus gpp = GriefPreventionPlus.getInstance();
                    Claim claim = gpp.getDataStore().getClaimAt(location, false, null);
                    
                    String[] delimit = sign.getLine(3).split(" ");
                    Double price = Double.valueOf(Double.valueOf(delimit[0].trim()).doubleValue());
                    
                    String status = ChatColor.stripColor(sign.getLine(1));
                    
                    if (claim == null){	// Sign is NOT inside a claim, breaks the sign.
                        player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "This sign is no longer within a claim!");
                        event.getClickedBlock().setType(Material.AIR);
                        return;
                    }
                	
                	if(event.getPlayer().isSneaking()){	// Player is sneaking, this is the info-tool
                		
                		String message = "";
                		
                		if(event.getPlayer().hasPermission("GPRealEstate.info")){

	                		String claimType = claim.getParent() == null ? "claim" : "subclaim";
	                		
	                		message += ChatColor.BLUE + "-----= " + ChatColor.WHITE + "[" + ChatColor.GOLD + "RealEstate Info" + ChatColor.WHITE + "]" + ChatColor.BLUE + " =-----\n";
	                		
	                		if(status.equalsIgnoreCase(plugin.dataStore.cfgReplaceSell)){
	                			message += ChatColor.AQUA + "This " + ChatColor.GREEN + claimType.toUpperCase() + ChatColor.AQUA + " is for sale, for " + ChatColor.GREEN + price + " " + GPPRealEstate.econ.currencyNamePlural() + "\n";
	                			if(claimType.equalsIgnoreCase("claim")){
	                				message += ChatColor.AQUA + "The current owner is: " + ChatColor.GREEN + claim.getOwnerName();
	                			}
	                			else {
	                				message += ChatColor.AQUA + "The main claim owner is: " + ChatColor.GREEN + claim.getOwnerName() + "\n";
	                				message += ChatColor.LIGHT_PURPLE + "Note: " + ChatColor.AQUA + "You will only buy access to this subclaim!"; 
	                			}
	                		}
	                		else if(claimType.equalsIgnoreCase("subclaim") && status.equalsIgnoreCase(plugin.dataStore.cfgReplaceRent)){
	                			message += ChatColor.AQUA + "This " + ChatColor.GREEN + claimType.toUpperCase() + ChatColor.AQUA + " is for lease, for " + ChatColor.GREEN + price + " " + GPPRealEstate.econ.currencyNamePlural() + "\n";
	                			message += ChatColor.AQUA + "The leasing period has to be renewed every " + ChatColor.GREEN + "X days";
	                		}
	                		else {
	                			message = ChatColor.RED + "Ouch! Something went wrong!";
	                		}
                		
                		}
                		else {
                			message = ChatColor.RED + "You do not have permissions to get RealEstate info!";
                		}
                		
                		event.getPlayer().sendMessage(message);
                		
                	}
                	else {
                		
                		// Player is not sneaking, and wants to buy/lease the claim
                		if(claim.getOwnerName().equalsIgnoreCase(player.getName())) {
                        	player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You already own this claim!");
                            return;
                        }
                		
                		@SuppressWarnings("deprecation")
						OfflinePlayer owner = Bukkit.getOfflinePlayer(sign.getLine(2));
                		
                		if(owner == null || !owner.getUniqueId().equals(claim.getParent()==null ? claim.getOwnerID() : claim.getParent().getOwnerID())) {
                            player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "The listed player does not have the rights to sell/lease this claim!");
                            event.getClickedBlock().setType(Material.AIR);
                            return;
                        }
                		if(claim.isAdminClaim()) {
                            player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "Admin claims can't be !");
                            event.getClickedBlock().setType(Material.AIR);
                            return;
                        }
                		
                		if(claim.getParent() == null){
                			// This is a normal claim.
                			if (GPPRealEstate.perms.has(player, "GPRealEstate.claim.buy")) {
	                			
                				if((claim.getArea() <= gpp.getDataStore().getPlayerData(player.getUniqueId()).getAccruedClaimBlocks()) || player.hasPermission("GPRealEstate.ignore.limit")){
                					
                					if(makePayment(player, owner, price)){
                					
		                                try {
		
		                                    for (Claim child : claim.getChildren()) {
		                                        child.clearPermissions();
		                                        // child.managers.remove(child.getOwnerName());
		                                    }
		
		                                    claim.clearPermissions();
		                                    gpp.getDataStore().changeClaimOwner(claim, player.getUniqueId());
		
		                                }
		                                catch (Exception e) {
		                                    e.printStackTrace();
		                                    return;
		                                }
		
		                                if (claim.getOwnerName().equalsIgnoreCase(player.getName())) {
		                                	
		                                    player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.AQUA + "You have successfully purchased this claim for " + ChatColor.GREEN + price + GPPRealEstate.econ.currencyNamePlural());
		                                    plugin.addLogEntry(
		                                    	"[" + this.dateFormat.format(this.date) + "] " + player.getName() + " Has purchased a claim at "
		                                    	+ "[" + player.getLocation().getWorld() + ", "
		                                    	+ "X: " + player.getLocation().getBlockX() + ", "
		                                    	+ "Y: " + player.getLocation().getBlockY() +", "
		                                    	+ "Z: " + player.getLocation().getBlockZ() + "] "
		                                    	+ "Price: " + price + " " + GPPRealEstate.econ.currencyNamePlural()
		                                    );
		                                    
		                                } else {
		                                    player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "Cannot purchase claim!");
		                                    return;
		                                }
		
		                                //gp.dataStore.saveClaim(claim);
		                                event.getClickedBlock().setType(Material.AIR);
		                                return;
	                                
                					}
	
	                            }
	                            else {
	                                player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You do not have enough claim blocks available.");
	                                return;
	                            }
                			
                			}
                			else {
                				player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You do not have permission to buy claims!");
                				return;
                			}
                				
                		}
                		else {
                			
                			// This is a subclaim.
                			if(status.equalsIgnoreCase(plugin.dataStore.cfgReplaceSell)){
                				
                				if (GPPRealEstate.perms.has(player, "GPRealEstate.subclaim.buy")) {
                					
                					
                					if(makePayment(player, owner, price)){

	                                    claim.clearPermissions();
	
	                                    /*if (!sign.getLine(2).equalsIgnoreCase("server")) {
	                                       claim.managers.remove(sign.getLine(2));
	                                    }*/

	                                    claim.setPermission(player.getUniqueId(), ClaimPermission.MANAGE);	// Allowing the player to manage permissions.
	                                    claim.setPermission(player.getUniqueId(), ClaimPermission.BUILD);	// Allowing the player to build in the subclaim!
	                                    event.getClickedBlock().breakNaturally();
	
	                                    player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.AQUA + "You have successfully purchased this subclaim for " + ChatColor.GREEN + price + GPPRealEstate.econ.currencyNamePlural());
	
	                                    plugin.addLogEntry(
	                                    	"[" + this.dateFormat.format(this.date) + "] " + player.getName() + " Has purchased a subclaim at "
	                                    	+ "[" + player.getLocation().getWorld() + ", "
	                                    	+ "X: " + player.getLocation().getBlockX() + ", "
	                                    	+ "Y: " + player.getLocation().getBlockY() + ", "
	                                    	+ "Z: " + player.getLocation().getBlockZ() + "] "
	                                    	+ "Price: " + price + " " + GPPRealEstate.econ.currencyNamePlural()
	                                    );
	                                    return;
                                    
                					}

                                } else {
                                    player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You do not have permission to buy subclaims!");
                                    return;
                                }
                				
                			}
                			else if(status.equalsIgnoreCase(plugin.dataStore.cfgReplaceSell) && plugin.dataStore.cfgEnableLeasing){
                				
                				// Leasing subclaims
                				
                				if (GPPRealEstate.perms.has(player, "GPRealEstate.subclaim.buy")) {
                					player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.DARK_PURPLE + "The leasing function is currently being worked on!");
                				}
                				else {
                                    player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "You do not have permission to lease subclaims!");
                                    return;
                                }
                				
                				//if(period.matches("^([0-9]{1,3})(w{1})$")){
                				//	lengthInSeconds = ((7*24*60*60)*length); 	// The time has been set using weeks.
                				//}
                				//else if(period.matches("^([0-9]{1,3})(d{1})$")){
                				//	lengthInSeconds = ((24*60*60)*length); 		// The time has been set using days.
                				//}
                				//else if(period.matches("^([0-9]{1,3})(h{1})$")){
                				//	lengthInSeconds = ((60*60)*length); 		// The time has been set using hours.
                				//}
                				
                			}
                			else {
                				player.sendMessage(plugin.dataStore.chatPrefix + ChatColor.RED + "This sign was misplaced!");
                                event.getClickedBlock().setType(Material.AIR);
                                return;
                			}
                            
                		} // END IF CHECK CLAIM TYPE
                        
                        //if(claim.get)
                		
                	}
                	
                } // END IF CHECK GPRE SIGN
                
            } // END IF SIGN CHECK
    		
    	} // END IF RIGHT CLICK CHECK
    	
    }
    
}