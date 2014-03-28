package com.github.etsija.capturetheflag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CaptureTheFlag extends JavaPlugin {

	private Logger _log = Logger.getLogger("Minecraft"); // Write debug info to console
	File configFile;					// config.yml
	FileConfiguration config;			// configuration object for config.yml
	private String world;				// 
	private Integer checkInterval;		// plugin config parameters
	private Integer materialId;			//
	private Timer timer = new Timer();	// Timer task
	
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
		configParams.put("checkInterval", "3");
		configParams.put("materialId", "399");
		setDefaultValues(config, configParams);
				
		// And save them to the files, if they don't already contain such parameters
		// This is also a great way to correct mistyping of the config params (by the users)
		saveYaml(configFile, config);
				
		// Finally, import all needed config params from the corresponding config files
		world = config.getString("world");
		checkInterval = config.getInt("checkInterval");
		materialId = config.getInt("materialId");
		
		// Initialize the check loop (timer task)
        // Multiply by 1000 because Timer accepts its arguments in milliseconds...
		_log.info("[CaptureTheFlag] Initializing timer loop...");
        this.timer.scheduleAtFixedRate(new CaptureTheFlagTimer(this,_log, world, materialId),
                                        checkInterval*1000,
                                        checkInterval*1000);
		
		_log.info("[CaptureTheFlag] enabled!");
	}
	
	public void onDisable() {
	
		saveYaml(configFile, config);
		
		// Cancel all CaptureTheFlagTimer tasks
		this.timer.cancel();
		
		_log.info("[CaptureTheFlag] disabled!");
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
