package com.github.etsija.capturetheflag;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class CaptureTheFlagTimer extends BukkitRunnable {

	Logger _log;
	private CaptureTheFlag _plugin;
	private String _world;
	private Integer _materialId;
	List<String> _whoHaveFlags = new ArrayList<String>();
	
	// Constructor
	public CaptureTheFlagTimer(CaptureTheFlag plugin,
							   Logger log,
							   String world,
							   Integer materialId) {
		this._plugin = plugin;
		this._log = log;
		this._world = world;
		this._materialId = materialId;
	}
			
	// Handle all online players when the task runs as scheduled
	@Override
	public void run() {
		for (Player player : this._plugin.getServer().getOnlinePlayers()) {
			this._handlePlayer(player);
		}
	}

	// This method checks one player at a time for the flag capture
    private void _handlePlayer(Player player) {
    	
        // If this player is not in the world to be checked -> return immediately
        String playerWorld = player.getWorld().getName();
        if (!playerWorld.equals(_world)) {
        	return;
        }

        String playerName = player.getDisplayName().toLowerCase();
        	
        // Check if this player has the flag
        String name = checkItem(player, _materialId);
        if (name.equalsIgnoreCase("blue") || name.equalsIgnoreCase("yellow"))
        {
        	// If the player just took the flag...
        	if (!_whoHaveFlags.contains(playerName)) {
        		_whoHaveFlags.add(playerName);
        			
       			// Blue team got back its own plugin
       			if (name.equals("blue") && _plugin.teamBlue.contains(playerName)) {
       				Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName
        					                + ChatColor.WHITE + " is now carrying the"
        					                + ChatColor.BLUE  + " blue team's"
        					                + ChatColor.WHITE + " own flag");
        		} else if (name.equals("yellow") && _plugin.teamYellow.contains(playerName)) {
        			Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName
			                                + ChatColor.WHITE   + " is now carrying the"
			                                + ChatColor.YELLOW  + " yellow team's"
			                                + ChatColor.WHITE   + " own flag");
        		} else if (name.equals("blue") && _plugin.teamYellow.contains(playerName)) {
        			Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName
                                            + ChatColor.WHITE + " has now captured"
                                            + ChatColor.BLUE  + " blue team's flag!");
        		} else if (name.equals("yellow") && _plugin.teamBlue.contains(playerName)) {
        			Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName
                              				+ ChatColor.WHITE  + " has now captured"
                               				+ ChatColor.YELLOW + " yellow team's flag!");
        		}
        	}
        	// Play the chosen effect
        	if (_plugin.effectInUse.equals("lightning")) {
        		player.getWorld().strikeLightningEffect(player.getLocation());
        	} else if (_plugin.effectInUse.equals("flames")) {
        		player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
        	} else if (_plugin.effectInUse.equals("fireworks")) {
        		//firework(player);
        	} else if (_plugin.effectInUse.equals("endereye")) {
        		player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 1);
        	} else if (_plugin.effectInUse.equals("smoke")) {
        		player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 1);
        	}

        } else {
        		
        	// If the player dropped the flag
        	if (_whoHaveFlags.contains(playerName)) {
        		double X = player.getLocation().getX();
        		double Z = player.getLocation().getZ();
        		_whoHaveFlags.remove(playerName);	
        		Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName 
						                + ChatColor.WHITE + " dropped the flag at [" + (int)X + "," + (int)Z + "]");
        	}
        }
    }
    
    // Method to determine if the player has a named item in his inventory
    @SuppressWarnings("deprecation")
	private String checkItem(Player p, Integer materialId) {
    	String str = "";
    	PlayerInventory pi = p.getInventory();
    	
    	if (!pi.contains(materialId)) {
    		return "";
    	}
    	
    	for (int i = 0; i < pi.getSize(); i++) {
    		if (pi.getItem(i) != null) {
    			if (pi.getItem(i).getItemMeta().hasDisplayName()) {
    				str = pi.getItem(i).getItemMeta().getDisplayName().toLowerCase();
    			}
    		}
    	}
    	return str;
    }
    
    // Method to launch fireworks on player location
    private void firework(Player player) {
    	Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwmeta = fw.getFireworkMeta();
        FireworkEffect.Builder builder = FireworkEffect.builder();
        builder.withTrail();
        builder.withFlicker();
        builder.withFade(Color.GREEN);
        builder.withColor(Color.WHITE);
        builder.withColor(Color.YELLOW);
        builder.withColor(Color.BLUE);
        builder.withColor(Color.FUCHSIA);
        builder.withColor(Color.PURPLE);
        builder.withColor(Color.MAROON);
        builder.withColor(Color.LIME);
        builder.withColor(Color.ORANGE);
        //builder.with(FireworkEffect.Type.BALL_LARGE);
        fwmeta.addEffect(builder.build());
        fwmeta.setPower(1);
        fw.setFireworkMeta(fwmeta);
    }
	
}
