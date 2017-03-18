package me.DiscoDuck.DiscoReview;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscoReview extends JavaPlugin
{
  private File file = new File("plugins/DiscoReview", "config.yml");
  private FileConfiguration cfg = YamlConfiguration.loadConfiguration(this.file);

  public void onEnable() {
    this.cfg = getConfig();
    saveConfig();
    getLogger().info("[DiscoReview] The plugin has been enabled.");
  }

  public void onDisable()
  {
    getLogger().info("[DiscoReview] The plugin has been disabled.");
  }

  public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
  {
    if (!(sender instanceof Player)) // Commands can not be ran by console.
    {
      sender.sendMessage("[DiscoReview] Commands may only be ran by in-game players.");
      return true;
    }
    if (commandLabel.equalsIgnoreCase("review")) // The base command for the plugin. All arguments are stem from this command
    {
      Player player = (Player) sender;
      Location loc = player.getLocation();
      String name = player.getName();
      if (args.length == 0)
      {
        sender.sendMessage(ChatColor.RED + "Type " + ChatColor.DARK_RED + "/review help " + ChatColor.RED + "for further information.");
        return true;
      }
      if (args[0].equalsIgnoreCase("help")) // Messages the player a list of available commands
      {
        sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------------");
        sender.sendMessage(ChatColor.DARK_GREEN + "/review support " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Request Administrative Support");
        sender.sendMessage(ChatColor.DARK_GREEN + "/review submit " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Request A Plot Review");
        sender.sendMessage(ChatColor.DARK_GREEN + "/review cancel " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Cancel A Plot Review");
        sender.sendMessage(ChatColor.DARK_GREEN + "/review goto " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Teleport To A Submitted Plot");
        sender.sendMessage(ChatColor.DARK_GREEN + "/review accept " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Accept The Current Plot");
        sender.sendMessage(ChatColor.DARK_GREEN + "/review decline " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Decline The Current Plot");
        sender.sendMessage(ChatColor.DARK_GREEN + "/review status " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Check Your Review Status");
        sender.sendMessage(ChatColor.DARK_GREEN + "/review list " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Check The List Of Submits");
        sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------------");

        return true;
      }
      if ((args[0].equalsIgnoreCase("list")) && (player.hasPermission("review.list"))) // Messages the player a list of players who have yet to have their plots reviewed
      {
    	  List<String> submits = this.cfg.getStringList("list");
		  sender.sendMessage(ChatColor.GREEN + "The players who have yet to have their plot reviewed are:");
    	  for (String submit : submits)
    		  sender.sendMessage(submit);

    	  return true;
      }
      if (args[0].equalsIgnoreCase("status")) // Messages the player if their plot has been accepted or declined or a decision has yet to be made
      {
        if (this.cfg.contains("rm." + name))
        {
          double x = this.cfg.getDouble("rm." + name + ".accept");
          if (x == 1.0D)
          {
            sender.sendMessage(ChatColor.DARK_GREEN + "Current Status " + ChatColor.GRAY + "- " + ChatColor.GREEN + ChatColor.BOLD + "[A] " + ChatColor.RESET + ChatColor.GRAY + "Accepted!");
            return true;
          }
          if (x == 2.0D)
          {
            sender.sendMessage(ChatColor.DARK_GREEN + "Current Status " + ChatColor.GRAY + "- " + ChatColor.RED + ChatColor.BOLD + "[D] " + ChatColor.RESET + ChatColor.GRAY + "Declined. Please try again, type /review cancel to reset.");
            return true;
          }
          sender.sendMessage(ChatColor.DARK_GREEN + "Current Status " + ChatColor.GRAY + "- " + ChatColor.YELLOW + ChatColor.BOLD + "[P] " + ChatColor.RESET + ChatColor.GRAY + "Your plot is currently waiting for a result.");
          return true;
        }
        sender.sendMessage(ChatColor.RED + "You have not requested a plot review. Type /review submit to request a plot review.");
        return true;
      }
      if ((args[0].equalsIgnoreCase("support")) && (player.hasPermission("review.support"))) // Messages an online admin for assistance
      {
        sender.sendMessage(ChatColor.GREEN + "You have successfully requested support. An online administrator has been alerted.");
        Bukkit.broadcast(ChatColor.RED + name + " has requested support. Type /tp to teleport to them.", "review.support");
        return true;
      }
      if ((args[0].equalsIgnoreCase("cancel")) && (player.hasPermission("review.review"))) // Cancels your submitted plot from the list
      {
        this.cfg.set("rm." + name, null);
        List <String> submits = this.cfg.getStringList("list");
        if (submits.contains(player.getName()))
        {
        	submits.remove(player.getName());
        	this.cfg.set("list", submits);
        }     	
        saveConfig();
        sender.sendMessage(ChatColor.GREEN + "You have successfully removed your plot from the review list.");
        if (player.hasPermission("review.alert"))
        {
          player.sendMessage(ChatColor.RED + name + " has canceled their plot review request.");
          return true;
        }
        return true;
      }
      if ((args[0].equalsIgnoreCase("submit")) && (player.hasPermission("review.review"))) // Submits the plot to be reviewed
      {
        this.cfg.set("rm." + name + ".complete", null);
        this.cfg.set("rm." + name + ".world", loc.getWorld().getName().toString());
        this.cfg.set("rm." + name + ".x", Double.valueOf(loc.getX()));
        this.cfg.set("rm." + name + ".y", Double.valueOf(loc.getY()));
        this.cfg.set("rm." + name + ".z", Double.valueOf(loc.getZ()));
        this.cfg.set("rm." + name + ".yaw", Float.valueOf(loc.getYaw()));
        this.cfg.set("rm." + name + ".pitch", Float.valueOf(loc.getPitch()));
        List <String> submits = this.cfg.getStringList("list");
        if (!submits.contains(player.getName()))
        	submits.add(player.getName());
        this.cfg.set("list", submits);
        saveConfig();
        sender.sendMessage(ChatColor.GREEN + "You have successfully requested a plot review.");
        Bukkit.broadcast(ChatColor.RED + name + " has submitted their plot for review. Type /review goto [name] to teleport to the player.", "review.alert");
        return true;
      }
      if (args.length == 2)
      {
        if ((args[0].equalsIgnoreCase("goto")) && (player.hasPermission("review.goto"))) // Teleports you to the players' submit location
        {
          if (this.cfg.contains("rm." + args[1] + ".x"))
          {
            World w = Bukkit.getWorld(this.cfg.getString("rm." + args[1] + ".world"));
            double x = this.cfg.getDouble("rm." + args[1] + ".x");
            double y = this.cfg.getDouble("rm." + args[1] + ".y");
            double z = this.cfg.getDouble("rm." + args[1] + ".z");
            float yaw = (float)this.cfg.getDouble("rm." + args[1] + ".yaw");
            float pitch = (float)this.cfg.getDouble("rm." + args[1] + ".pitch");
            Location loc1 = new Location(w, x, y, z, yaw, pitch);
            player.teleport(loc1);
            sender.sendMessage(ChatColor.GRAY + "Teleporting you to " + args[1] + "'s plot!");
            sender.sendMessage(ChatColor.GRAY + "To confirm you have been do " + ChatColor.GREEN + "/review <accept|decline> <username>");
            saveConfig();
            return true;
          }
          sender.sendMessage(ChatColor.RED + args[1] + " has not requested a plot review.");
          return true;
        }
        if ((args[0].equalsIgnoreCase("decline")) && (player.hasPermission("review.decline"))) // Declines a player's submitted plot
        {
          Object bless = Integer.valueOf(2);
          this.cfg.set("rm." + args[1], null);
          this.cfg.set("rm." + args[1] + ".complete", bless);
          List <String> submits = this.cfg.getStringList("list");
          if (submits.contains(player.getName()))
          {
          	submits.remove(player.getName());
          	this.cfg.set("list", submits);
          }
          saveConfig();
          sender.sendMessage(ChatColor.RED + args[1] + "'s plot has been declined.");
          Bukkit.getServer().getPlayer(args[1]).sendMessage(ChatColor.RED + "An admin has checked your plot. Your plot has been declined.");
          return true;
        }
        if ((args[0].equalsIgnoreCase("accept")) && (player.hasPermission("review.accept"))) //Accepts a player's submitted plot
        {
          Object yes = Integer.valueOf(1);
          this.cfg.set("rm." + args[1], null);
          this.cfg.set("rm." + args[1] + ".accept", yes);
          List <String> submits = this.cfg.getStringList("list");
          if (submits.contains(player.getName()))
          {
          	submits.remove(player.getName());
          	this.cfg.set("list", submits);
          }
          saveConfig();
          sender.sendMessage(ChatColor.GREEN + args[1] + "'s plot has been accepted.");
          Bukkit.getServer().getPlayer(args[1]).sendMessage(ChatColor.RED + "An admin has checked your plot. Your plot has been accepted!");

          return true;
        }
      }
    }
	return false;
  }
}


	

      

      

