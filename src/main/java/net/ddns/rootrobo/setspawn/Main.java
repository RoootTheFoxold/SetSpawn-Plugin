package net.ddns.rootrobo.setspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Main extends JavaPlugin implements Listener {

    Logger LOGGER = getLogger();

    public File configfile = new File(getDataFolder(), "config.yml");
    public FileConfiguration config = YamlConfiguration.loadConfiguration(configfile);

    private Location getSpawn(FileConfiguration config) {
        double X = config.getDouble("X");
        double Y = config.getDouble("Y");
        double Z = config.getDouble("Z");

        String pitchS = config.getString("pitch");
        String yawS = config.getString("yaw");

        if (pitchS== null || pitchS.equals("")) {
            LOGGER.warning("Pitch is not set, using 0.");
            pitchS = "0";
        }
        if (yawS == null || yawS.equals("")) {
            LOGGER.warning("Yaw is not set, using 0.");
            yawS = "0";
        }
        String worldName = config.getString("World");
        if (worldName == null || worldName.equals("")) {
            World newWorld = getServer().getWorlds().get(0);
            LOGGER.warning("World is not set, using \"" + newWorld.getName() +"\".");
            worldName = newWorld.getName();
        }
        World world = Bukkit.getWorld(worldName);
        float pitch = Float.parseFloat(pitchS);
        float yaw = Float.parseFloat(yawS);

        return new Location(world, X, Y, Z, yaw, pitch);
    }

    private void setSpawn(FileConfiguration config, Location location) {
        double X = location.getX();
        double Y = location.getY();
        double Z = location.getZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        World world = location.getWorld();
        String worldName;
        if (world != null) {
            worldName = world.getName();
        } else {
            worldName = getServer().getWorlds().get(0).getName();
            LOGGER.warning("Unknown World! Using \"" + worldName + "\".");
        }
        config.set("X", X);
        config.set("Y", Y);
        config.set("Z", Z);
        config.set("pitch", pitch);
        config.set("yaw", yaw);
        config.set("World", worldName);
        try {
            config.save(configfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {

        LOGGER.info("Loading config files...");

        config.addDefault("messages.prefix", "§f[SetSpawn]§r ");
        config.addDefault("messages.spawn_success", "§aYou have been teleported to spawn.");
        config.addDefault("messages.no_permission", "§cYou don't have permission to use this command!");
        config.addDefault("messages.no_player", "§cOnly players can use this command!");
        config.addDefault("messages.setspawn_success", "§aSpawn set successfully!");
        config.addDefault("messages.reload_success", "§aConfig Reloaded!");

        config.options().copyDefaults(true);

        try {
            config.save(configfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Config files loaded!");
        getServer().getPluginManager().registerEvents(this, this);

        LOGGER.info(ChatColor.GREEN + "Enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("align")) {
            if(!(sender instanceof Player)){
                String message = config.getString("messages.prefix") + config.getString("messages.no_player");
                if(message.equals("")) {
                    message = "§cOnly players can use this command!";
                }
                sender.sendMessage(message);
                return true;
            }

            Player player = (Player)sender;

            if(!player.hasPermission("setspawn.align")) {
                String message = config.getString("messages.prefix") + config.getString("messages.no_permission");
                if(message.equals("")) {
                    message = "§cYou don't have permission to use this command!";
                }
                player.sendMessage(message);
                return true;
            }

            Location location = player.getLocation();
            World world = location.getWorld();
            int X = location.getBlockX();
            int Y = location.getBlockY();
            int Z = location.getBlockZ();
            float pitch = 0;
            float yaw = 0;

            double newX = Double.parseDouble(String.valueOf(X)) + 0.5;
            double newY = Double.parseDouble(String.valueOf(Y));
            double newZ = Double.parseDouble(String.valueOf(Z)) + 0.5;

            Location newLocation = new Location(world, newX, newY, newZ, yaw, pitch);
            player.teleport(newLocation);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setspawn")) {
            if(args.length >= 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("setspawn.reload")) {
                        String message = config.getString("messages.prefix") + config.getString("messages.no_permission");
                        if (message.equals("")) {
                            message = "§cYou don't have permission to use this command!";
                        }
                        sender.sendMessage(message);
                        return true;
                    }

                    try {
                        config.load(configfile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InvalidConfigurationException e) {
                        e.printStackTrace();
                    }
                    String message = config.getString("messages.prefix") + config.getString("messages.reload_success");
                    if(message.equals("")) {
                        message = "§aConfig Reloaded!";
                    }
                    sender.sendMessage(message);
                    return true;
                }
            }

            if(!(sender instanceof Player)){
                String message = config.getString("messages.prefix") + config.getString("messages.no_player");
                if(message.equals("")) {
                    message = "§cOnly players can use this command!";
                }
                sender.sendMessage(message);
                return true;
            }
            Player player = (Player)sender;

            if(!player.hasPermission("setspawn.setspawn")) {
                String message = config.getString("messages.prefix") + config.getString("messages.no_permission");
                if(message.equals("")) {
                    message = "§cYou don't have permission to use this command!";
                }
                player.sendMessage(message);
                return true;
            }

            Location location = player.getLocation();
            setSpawn(config, location);
            String message = config.getString("messages.prefix") + config.getString("messages.setspawn_success");
            if(message.equals("")) {
                message = "§cOnly players can use this command!";
            }
            player.sendMessage(message);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("spawn")) {
            if(!(sender instanceof Player)){
                String message = config.getString("messages.prefix") + config.getString("messages.no_player");
                if(message.equals("")) {
                    message = "§cOnly players can use this command!";
                }
                sender.sendMessage(message);
                return true;
            }
            Player player = (Player)sender;
            if(!player.hasPermission("setspawn.spawn")) {
                String message = config.getString("messages.prefix") + config.getString("messages.no_permission");
                if(message.equals("")) {
                    message = "§cYou don't have permission to use this command!";
                }
                player.sendMessage(message);
                return true;
            }

            Location Spawn = getSpawn(config);
            player.teleport(Spawn);

            String message = config.getString("messages.prefix") + config.getString("messages.spawn_success");
            if(message.equals("")) {
                message = "§cYou have been teleported to spawn.";
            }
            player.sendMessage(message);

            return true;

        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Location Spawn = getSpawn(config);
        event.getPlayer().teleport(Spawn);
    }

}
