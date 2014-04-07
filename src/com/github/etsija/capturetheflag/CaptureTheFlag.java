package com.github.etsija.capturetheflag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class CaptureTheFlag extends JavaPlugin {

	private Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	File configFile;						// config.yml
	FileConfiguration config;				// configuration object for config.yml
	private String world;					// 
	private Integer checkInterval = 3;		// plugin config parameters
	private Integer materialId;				//
	private BukkitTask timerTask;			// Timer task
	private boolean tracking = false;
	String strEffects[] = {"flames", "lightning", "endereye", "smoke"};
	List<String> effects = Arrays.asList(strEffects);
	String effectInUse = effects.get(0);
	List<String> teamBlue = new ArrayList<String>();
	List<String> teamYellow = new ArrayList<String>();
	
	public void onEnable() {
			
		// Initialize the configuration files
		// Note that so far, they're only virtual, not real files yet
		configFile = new File(getDataFolder(), "config.yml");
		
		// If the plugin is run the first time, create the actual config files
		try {
			firstRun(configFile, "config.yml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Import configurations from the (physical) files
		config = new YamlConfiguration();
		loadYaml(configFile, config);
		        
		// Set the default parameter values
		final Map<String, Object> configParams = new HashMap<String, Object>();
		configParams.put("world", "Seikkailumaa");
		configParams.put("materialId", "399");
		setDefaultValues(config, configParams);
				
		// And save them to the files, if they don't already contain such parameters
		// This is also a great way to correct mistyping of the config params (by the users)
		saveYaml(configFile, config);
				
		// Finally, import all needed config params from the corresponding config files
		world = config.getString("world");
		materialId = config.getInt("materialId");
				
		_log.info("[CaptureTheFlag] enabled!");
	}
	
	public void onDisable() {
	
		saveYaml(configFile, config);
		
		// Cancel all CaptureTheFlagTimer tasks
		//this.timer.cancel();
		
		_log.info("[CaptureTheFlag] disabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// Command /flag
		if ((cmd.getName().equalsIgnoreCase("flag")) &&
			(sender.hasPermission("capturetheflag.flag")) &&
			(args.length > 0)) {
			
			// /flag on || /flag on [interval]
			if (args[0].equalsIgnoreCase("on")) {
				// Initialize the check loop (timer task)
			    // Multiply by 1000 because Timer accepts its arguments in milliseconds...
				if (args.length == 2) {
					checkInterval = Integer.valueOf(args[1]);
					if (checkInterval < 1)
						checkInterval = 1;
				} else {
					checkInterval = 3;
					sender.sendMessage("[CTF] Flag polling interval now set to 3 seconds.");
					sender.sendMessage("[CTF] You can change it with /flag on [interval], minimum is 1s");
				}
				if (!tracking) {
					//this.timer = new Timer();
					//this.timer.scheduleAtFixedRate(new CaptureTheFlagTimer(this,_log, world, materialId),
					//							   checkInterval*1000,
                    //                               checkInterval*1000);
					timerTask = new CaptureTheFlagTimer(this, _log, world, materialId)
													   .runTaskTimer(this, 0, checkInterval*20);
					sender.sendMessage("[CTF] Flag following started.");
					tracking = true;
				} else {
					sender.sendMessage("[CTF] Already following the flag.");
				}
				return true;
			
			// /flag off
			} else if (args[0].equalsIgnoreCase("off")) {
				if (tracking) {
					timerTask.cancel();
					sender.sendMessage("[CTF] Flag following stopped.");
					tracking = false;
				} else {
					sender.sendMessage("[CTF] Was not following the flag.");
				}
				return true;
			
			// /flag effect || /flag effect [new effect]
			} else if (args[0].equalsIgnoreCase("effect")) {
				if (args.length == 1) {
					sender.sendMessage("[CTF] Current effect in use: " + effectInUse);
					sender.sendMessage("[CTF] Possible effects: " + effects);
					sender.sendMessage("[CTF] You can change the effect with /team effect [new effect]");
				} else if (effects.contains(args[1])) {
					effectInUse = args[1];
					sender.sendMessage("[CTF] You changed the effect to: " + effectInUse);
				}
				return true;
			}
		
		// Command /team
		} else if ((cmd.getName().equalsIgnoreCase("team")) &&
				(sender.hasPermission("capturetheflag.team")) &&
				(args.length > 0)) {
			
			// /team add || /team add [player] [team]
			if (args[0].equalsIgnoreCase("add")) {
				if (args.length < 3) {
					sender.sendMessage("[CTF] Usage: /team add [player] [blue/yellow]");
				} else {
			        // Check that the team is either blue or yellow
					String playerName = args[1].toLowerCase();
			        if (!(args[2].equalsIgnoreCase("blue") || args[2].equalsIgnoreCase("yellow"))) {
			        	sender.sendMessage("[CTF] Only blue and yellow teams allowed.");
			        	return true;
			        }
			        // Check that the player is not already on this or another team
			        if (teamBlue.contains(playerName) || teamYellow.contains(playerName)) {
			        	sender.sendMessage("[CTF] " + ChatColor.RED + playerName
			        					   + ChatColor.WHITE + " already in a team. Use /team remove/change.");
			        	return true;
			        }
			        // All is well, so add the player to a team
			        if (args[2].equalsIgnoreCase("blue")) {
			        	teamBlue.add(playerName);
			        	Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName 
			        			                + ChatColor.WHITE + " added to the"
			        			                + ChatColor.BLUE  + " blue team!");
			        } else {
			        	teamYellow.add(playerName);
			        	Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName 
			        			                + ChatColor.WHITE  + " added to the"
			        			                + ChatColor.YELLOW + " yellow team!");
			        }
				}
				return true;
			
			// /team remove || /team remove [player]
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (args.length < 2) {
					sender.sendMessage("[CTF] Usage: /team remove [player]");
				} else {
					String playerName = args[1].toLowerCase();
					if (teamBlue.contains(playerName)) {
						teamBlue.remove(playerName);
						Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName 
								                + ChatColor.WHITE + " removed from the"
								                + ChatColor.BLUE  + " blue team!");
					} else if (teamYellow.contains(playerName)) {
						teamYellow.remove(playerName);
						Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName 
				                                + ChatColor.WHITE   + " removed from the"
				                                + ChatColor.YELLOW  + " yellow team!");
					} else {
						sender.sendMessage("[CTF] " + playerName + " doesn't seem to belong to either team.");
					}
				}
				return true;
				
			// /team change || /team change [player]
			} else if (args[0].equalsIgnoreCase("change")) {
				if (args.length < 2) {
					sender.sendMessage("[CTF] Usage: /team change [player]");
				} else {
					String playerName = args[1].toLowerCase();
					if (teamBlue.contains(playerName)) {
						teamBlue.remove(playerName);
						teamYellow.add(playerName);
						Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName
												+ ChatColor.WHITE  + " switched from the"
												+ ChatColor.BLUE   + " blue team"
												+ ChatColor.WHITE  + " to the"
												+ ChatColor.YELLOW + " yellow team!");
					} else if (teamYellow.contains(playerName)) {
						teamYellow.remove(playerName);
						teamBlue.add(playerName);
						Bukkit.broadcastMessage("[CTF] " + ChatColor.RED + playerName
												+ ChatColor.WHITE  + " switched from the"
												+ ChatColor.YELLOW + " yellow team"
												+ ChatColor.WHITE  + " to the"
												+ ChatColor.BLUE   + " blue team!");
					} else {
						sender.sendMessage("[CTF] " + playerName + " doesn't seem to belong to either team.");
					}
				}
				return true;
				
			// /team list || /team list [blue/yellow]
			} else if (args[0].equalsIgnoreCase("list")) {
				if (args.length < 2) {
					sender.sendMessage("[CTF] " + ChatColor.BLUE + "Blue team: " + teamBlue);
					sender.sendMessage("[CTF] " + ChatColor.YELLOW + "Yellow team: " + teamYellow);
				} else if (args[1].equalsIgnoreCase("blue")) {
					sender.sendMessage("[CTF] " + ChatColor.BLUE + "Blue team: " + teamBlue);
				} else if (args[1].equalsIgnoreCase("yellow")) {
					sender.sendMessage("[CTF] " + ChatColor.YELLOW + "Yellow team: " + teamYellow);
				}
				return true;
			}
		}
		return false;
	}
	
	//////////////////////////////////////
	// Plugin's file configuration methods
	//////////////////////////////////////
	
	// Set default values for parameters if they don't already exist
	public void setDefaultValues(FileConfiguration config, Map<String, Object> configParams) {
		if (config == null) return;
		for (final Entry<String, Object> e : configParams.entrySet())
			if (!config.contains(e.getKey()))
				config.set(e.getKey(), e.getValue());
	}
	
	// Load a file from disk into its respective FileConfiguration
	public void loadYaml(File file, FileConfiguration configuration) {
        try {
            configuration.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save a FileConfiguration into its respective file on disk
    public void saveYaml(File file, FileConfiguration configuration) {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	// This private method is called on first time the plugin is executed
	// and it handles the creation of the datafiles
	private void firstRun(File file, String filename) throws Exception {
	    if (!file.exists()) {
	        file.getParentFile().mkdirs();
	        copy(getResource(filename), file);
	    }
	}
	
	// This is a private method to copy contents of the YAML file found in
	// the JAR to a datafile in ./pluginname/*.yml
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0)
	            out.write(buf,0,len);
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
