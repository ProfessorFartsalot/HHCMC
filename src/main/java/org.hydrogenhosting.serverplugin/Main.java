package org.hydrogenhosting.serverplugin;
import org.bukkit.plugin.java.JavaPlugin; // Required for the plugin to exist
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.bukkit.command.Command; // Required for command functions
import org.bukkit.command.CommandSender; // Required to know who sent what
import org.bukkit.entity.Player; // Required to find players

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey; // used for keys
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
public class Main extends JavaPlugin {
	Logger logger;
    @Override
    public void onEnable() {
        getLogger().info("Plugin enabled!");
        // Schedule the repeating tick task
        new BukkitRunnable() {
            @Override
            public void run() {
                tickGMParticlesAsync();
            }
        }.runTaskTimer(this, 0L, 1L); // Run every tick
        logger = getLogger();
    }
    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("operator")) {
        	if(!(sender.isOp())) {
        		sender.sendMessage("You are not an operator! You shall not pass!");
        		return false;
        	}
        	int x = Integer.parseInt(args[0]);
            logger.info("Calling sudo with " + x);
            switch (x) {
            	case 0: {
            		sender.sendMessage("FUCK");
            		break;
            	}
            	case 1: {
            		EmitBGMsByScore();
            		break;
            	}
            	case 2:{
            		Player player = (Player) sender;
            		player.setVelocity(new Vector(0.0f, 9999.0f, 0.0f));
            		break;
            	}
            	case 3:{
            		Player player = (Player) sender;
            		player.setVelocity(new Vector(0.0f, 0.0f, 0.0f));
            		break;
            	}
            	default: {
            		logger.warning("Attempting to call sudo with an unimplemented " + x);
            		break;
            	}
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("gm")) {
        	if (!sender.hasPermission("hhcsrv.mod") && !sender.hasPermission("hhcsrv.admin")) {
    			sender.sendMessage("You do not have permission to execute GM commands.");
    			return false;
    		}
        	String subCmd = args[0].toString();
        	Player player = (Player) sender;
        	if (subCmd.equalsIgnoreCase("particle")) {
        		int value = Integer.parseInt(args[1]);
        		if (value < 0 || value > 2) {
        			sender.sendMessage("Unsupported input: " + value);
        			return false;
        		}
        		Bukkit.getScoreboardManager().getMainScoreboard().getObjective("mod_particle").getScore(sender.getName()).setScore(value);
        		return true;
        	}
        	if (subCmd.equalsIgnoreCase("vanish")) {
        		Bukkit.dispatchCommand(sender, "sv");
        		Bukkit.getScoreboardManager().getMainScoreboard().getObjective("mod_particle").getScore(sender.getName()).setScore(0);
        		return true;
        	}
        	if (subCmd.equalsIgnoreCase("fly")) {
        		if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                    player.setAllowFlight(player.getAllowFlight() ? false : true);
                    sender.sendMessage(player.getAllowFlight() ? "Enabled GM flight" : "Disabled GM flight");
                }
        		else sender.sendMessage("You must be in survival mode to use GM flight");
        		return true;
        	}
        }
        return false;
    }

	public void EmitBGMsByScore() {
		for (Player player : Bukkit.getOnlinePlayers()) {
		    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		    Objective objective = scoreboard.getObjective("bgm_index");
		    if (objective == null) {
		        logger.warning("Objective bgm_index was not found!");
		        return;
		    }
		    Score score = objective.getScore(player.getName());
	        if (score.isScoreSet()) {
	        	switch(score.getScore()) {
		        	case 0: {
		        		return;
		        	}
		        	case 1: {
		        		player.playSound(player.getLocation(), "minecraft:dovah.cc.landfall", 1.0f, 1.0f);
		        		break;
		        	}
		        	case 2: {
		        		player.playSound(player.getLocation(), "minecraft:custom.big_blue", 1.0f, 1.0f);
		        		break;
		        	}
		        	default:{
		        		logger.warning("BGM with index "+ score.getScore() + " was not found!");
		        		break;
		        	}
	        	}
	        }
		}
	}

	public void tickGMParticlesAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            List<Player> GM_Mod = new ArrayList<>();
            List<Player> GM_Admin = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                Objective objective = player.getScoreboard().getObjective("mod_particle");;
                if (objective == null ) continue;
                Score score = objective.getScore(player.getName());
                if (score != null && score.getScore() >= 2) {
                	if (player.hasPermission("hhcsrv.admin")) GM_Admin.add(player);
                	else if (player.hasPermission("hhcsrv.mod")) GM_Mod.add(player);
                }
                else if (score.getScore() < 2) {
                	if (GM_Admin.contains(player)) GM_Admin.remove(player);
                	else if (GM_Mod.contains(player)) GM_Mod.remove(player);
                }
            }
            Bukkit.getScheduler().runTask(this, () -> {
                for (Player player : GM_Mod) {
                    player.getWorld().spawnParticle(
                        Particle.DUST_COLOR_TRANSITION,
                        player.getLocation(),
                        10,           // count
                        0.2, 0.5, 0.2, // offsetX, offsetY, offsetZ (delta/spread)
                        0.0,          // extra (not used here)
                        new DustTransition(Color.GREEN, Color.AQUA, 1.25f) // particle data
                    );
                }
                for (Player player : GM_Admin) {
                   player.getWorld().spawnParticle(
                       Particle.DUST_COLOR_TRANSITION,
                       player.getLocation(),
                       10,           // count
                       0.2, 0.5, 0.2, // offsetX, offsetY, offsetZ (delta/spread)
                       0.0,          // extra (not used here)
                       new DustTransition(Color.RED, Color.BLUE, 1.5f) // particle data
                   );
               }
            });
        });
    }
}