package com.github.etsija.capturetheflag;

import java.util.TimerTask;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Player;

public class CaptureTheFlagTimer extends TimerTask {

	Logger _log;
	private CaptureTheFlag _plugin;
	private String _world;
	private Integer _materialId;
	
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
	public synchronized void run() {
		for (Player player : this._plugin.getServer().getOnlinePlayers()) {
			this._handlePlayer(player);
		}
	}

	// This method checks one player at a time for the flag capture
    @SuppressWarnings("deprecation")
	private void _handlePlayer(Player player) {
    	
    	// So someone doesn't mess with the Player while we're busy...
        synchronized(player) {
        	
        	// If this player is not in the world to be checked -> return immediately
        	String playerWorld = player.getWorld().getName();
        	if (!playerWorld.equals(_world)) {
        		return;
        	}
        	
        	// Check if this player has the flag
        	if (player.getInventory().contains(_materialId)) {
        		Bukkit.broadcastMessage("Player " + player.getDisplayName() + " has the flag!");
        		//player.getWorld().strikeLightningEffect(player.getLocation());
        		player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
        	}
        	
        }
    }
	
}
