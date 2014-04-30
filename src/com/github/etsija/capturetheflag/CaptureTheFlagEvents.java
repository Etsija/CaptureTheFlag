package com.github.etsija.capturetheflag;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CaptureTheFlagEvents implements Listener {

	@EventHandler
	public void onCaptureFlag(PlayerInteractEvent event) {
		// this is pending on Bukkit implementing an event for left-click of itemframe (currently missing)
		
		/*Player p = event.getPlayer();
		Block  b = event.getClickedBlock();
		if ((b != null) && 
			(b.getType() == Material.ITEM_FRAME) &&
			(event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			p.sendMessage("Player " + p + " clicked an item frame");
			
		}*/
	}
	
}
