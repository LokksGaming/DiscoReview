package me.DiscoDuck.DiscoReview;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscoReview extends JavaPlugin
{ 
  private File file = new File(getDataFolder(), "config.yml");
  private FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
  private FileConfiguration pdata = null;
  private File pdatafile = null;  
  public PlotAPI api;
  public void onEnable() {
  	PluginManager manager = Bukkit.getServer().getPluginManager();
  	final Plugin plotsquared = manager.getPlugin("PlotSquared");
  	if (plotsquared == null)
	{
		getLogger().info("[DiscoReview] Could not find PlotSquared! Disabling plugin...");
		manager.disablePlugin(this);
		return;
	}
  	if ((plotsquared != null) && !plotsquared.isEnabled())
	  {
	  	getLogger().info("[DiscoReview] PlotSquared not enabled! Disabling plugin...");
	  	manager.disablePlugin(this);
	  	return;
	  }
  	api = new PlotAPI();
	getpdata();
	savepdata();
	this.cfg = getConfig();
	this.cfg.options().copyDefaults(true);
	saveConfig();
    getLogger().info("[DiscoReview] The plugin has been enabled.");
  }
  
public void onDisable()
  {
    getLogger().info("[DiscoReview] The plugin has been disabled.");
  }
public void reloadpdata() {
	if (pdatafile == null) {
		pdatafile = new File(getDataFolder(), "playerdata.yml");
	}
	pdata = YamlConfiguration.loadConfiguration(pdatafile);
	}
public FileConfiguration getpdata() {
	if (pdata == null) {
		reloadpdata();
	}
	return pdata;
}
public void savepdata() {
	if (pdata == null || pdatafile == null) {
		return;
	}
	try {
		getpdata().save(pdatafile);
	} catch (IOException ex) {
		getLogger().log(Level.SEVERE, "Could not save data to " + pdatafile, ex);
	}
}
public static PlotId getPlotId(com.intellectualcrafters.plot.object.Location location)
{
	PlotArea area = location.getPlotArea();
	return area == null ? null : area.getPlotManager().getPlotId(area, location.getX(), location.getY(), location.getZ());
}

  public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
  {
    if (!(sender instanceof Player)) // Commands can not be ran by console.
    {
      sender.sendMessage("[DiscoReview] Commands may only be ran by in-game players.");
      return true;
    }
    if (commandLabel.equalsIgnoreCase("build")) // The base command for the plugin. All arguments stem from this command
    {
	  if (!(sender instanceof Player))
	  {
		  getLogger().info("You must be a player to run this command!");
		  return true;
	  }
	  Player player = (Player) sender;
	  if (api.wrapPlayer(player).getLocation().isPlotRoad())
	  {
		  player.sendMessage(ChatColor.DARK_RED + "You must be in a plot to use /build");
		  return true;
	  }
	  if (!(api.wrapPlayer(player).getCurrentPlot().getOwners().contains(player.getUniqueId())))
	  {
		  player.sendMessage(ChatColor.DARK_RED + "You must be the plot owner to use /build.");
		  return true;
	  }
	  Location loc = player.getLocation();
	  String playerplot = api.wrapPlayer(player).getCurrentPlot().getId().toString();
      List <String> submits = getpdata().getStringList("submitted");
      List <String> accepts = getpdata().getStringList("accepted");
      List <String> declines = getpdata().getStringList("declined");
      List <String> allsubmits = getpdata().getStringList("allsubmitted");
      List <String> world = getpdata().getStringList("worlds");
      List <String> plotIDs= getpdata().getStringList("plotIDs");
      String name = player.getName();
      int worldSize = world.size();
      int negworldSize = worldSize-2;
      if (args.length == 0)
      {
        sender.sendMessage(ChatColor.RED + "Type " + ChatColor.DARK_RED + "/build help " + ChatColor.RED + "for further information.");
        return true;
      }
      if (args[0].equalsIgnoreCase("help")) // Messages the player a list of available commands
      {
        sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------------");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build support " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Request Administrative Support");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build submit " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Request A Plot Review");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build cancel " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Cancel A Plot Review");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build goto " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Teleport To A Submitted Plot");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build accept " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Accept The Current Plot");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build decline " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Decline The Current Plot");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build status " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Check Your Review Status");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build list " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Check The Available Lists In Game");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build new " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Restart The Build Contest And Restore Permissions");
        sender.sendMessage(ChatColor.DARK_GREEN + "/build world " + ChatColor.GRAY + "- " + ChatColor.GREEN + "Add Worlds To Be Able To Submit In");
        sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------------");

        return true;
      }
      if ((args[0].equalsIgnoreCase("world")))
      {
		  if (!(sender.hasPermission("discoreview.world")))
		  {
			  sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
			  return true;
		  }
    	  if (args.length == 1)
    	  {
        	  sender.sendMessage(ChatColor.RED + "To add a world to be able to submit in, do /build world add [worldname]");
        	  return true;
    	  }
    	  if ((args[1].equalsIgnoreCase("add")))
    	  {
        	  if ((args.length == 3) && (!(world.contains(args[2]))))
        	  {
        		  world.add(args[2]);
        		  getpdata().set("worlds", world);
        		  savepdata();
        		  sender.sendMessage(ChatColor.GOLD + args[2] + ChatColor.GREEN + " has been added to the list of available submit worlds");
        		  return true;
        	  }
        	  if (((args.length == 3)) && (world.contains(args[2])))
        	  {
        		  sender.sendMessage(ChatColor.DARK_RED + args[2] + ChatColor.RED + " is already added");
        		  return true;
        	  }
    		  sender.sendMessage(ChatColor.RED + "To add a world to be able to submit in, do /build world add [worldname]");
    		  return true;
    	  }
    	  if ((args[1].equalsIgnoreCase("remove")))
    	  {
    		  if (((args[1].equalsIgnoreCase("remove")) && (args.length == 3)) && (world.contains(args[2])))
        	  {
        		  world.remove(args[2]);
        		  getpdata().set("worlds", world);
        		  savepdata();
        		  sender.sendMessage(ChatColor.GOLD + args[2] + ChatColor.GREEN + " has been removed from the list of available submit worlds");
        		  return true;
        	  }
    		  if (((args[1].equalsIgnoreCase("remove")) && (args.length == 3)) && (!(world.contains(args[2]))))
        	  {
        		  sender.sendMessage(ChatColor.DARK_RED + args[2] + ChatColor.RED + " has not been added to the list of available submit worlds");
        		  return true;
        	  }
    		  sender.sendMessage(ChatColor.RED + "To remove a world to be able to submit in, do /build world remove [world]");
    		  return true;
    	  }
    	  return true;
      }
      if ((args[0].equalsIgnoreCase("new")))
      {
    	  if (!(sender.hasPermission("discoreview.new")))
    	  {
    		  sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
    		  return true;
    	  }
    	  if (args.length == 1)
    	  {
    		  sender.sendMessage(ChatColor.RED + "Are you sure? This may cause lag. /build new confirm. To clear players' build data and plots.");
    		  return true;
    	  }
    	  if (args[1].equalsIgnoreCase("confirm"))
    	  {
		  	if (!(world.contains(null)))
		  	{
		  		Iterator<String> id = plotIDs.iterator();
				Iterator<String> it = allsubmits.iterator();
				while (it.hasNext())
				{
					Iterator<String> its = world.iterator();
					String submitting = it.next();
					String ids = id.next();
					while (its.hasNext())
					{
						String worldz = its.next();
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "plot " + ids + " delete");
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + submitting + " permission unset worldguard.build.* global " + worldz);
					}
				}
			}
			 if (world.contains(null))
			 {
				 player.sendMessage(ChatColor.RED + "Could not update permissions. No worlds have been added.");
				 return true;
			 }
			  getpdata().set("plotIDs", null);
			  getpdata().set("dr", null);
			  getpdata().set("allsubmitted", null);
			  getpdata().set("submitted", null);
			  getpdata().set("accepted", null);
			  getpdata().set("declined", null);
    		  savepdata();
    		  sender.sendMessage(ChatColor.GREEN + "Player's build data has been erased and permissions have been restored.");
    		  return true;
    	  }
    	  return true;
      }
      if ((args[0].equalsIgnoreCase("list"))) // Messages the player a choice of either submitted builds or accepted builds
      {
    	  if (!(sender.hasPermission("discoreview.list")))
    	  {
    		  sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
    		  return true;
    	  }
    	  if (args.length == 1)
    	  {
    		  sender.sendMessage(ChatColor.RED + "/build list submitted. or /build list accepted.");
    	  }
    	  else if ((args[1].equalsIgnoreCase("accepted")))
    	  {
    		  sender.sendMessage(ChatColor.GOLD + "The players who have had their plots accepted are:");
    		  for (String accept : accepts)
        		  sender.sendMessage(accept);
    		  savepdata();
    		  
    	  }
    	  else if ((args[1].equalsIgnoreCase("submitted")))
    	  {
		  sender.sendMessage(ChatColor.GOLD + "The players who have yet to have their plot reviewed are:");
    	  for (String submit : submits)
    		  sender.sendMessage(submit);
    	  savepdata();
    	  
    	  }

    	  return true;
      }
      if (args[0].equalsIgnoreCase("status")) // Messages the player if their plot has been accepted or declined or a decision has yet to be made
      {
    	if (!(sender.hasPermission("discoreview.review")))
    	{
    		sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
    		return true;
    	}
        if (getpdata().contains("dr." + name))
        {
          double x = getpdata().getDouble("dr." + name + ".accept");
          if (x == 1.0D)
          {
            sender.sendMessage(ChatColor.DARK_GREEN + "Current Status " + ChatColor.GRAY + "- " + ChatColor.GREEN + ChatColor.BOLD + "[A] " + ChatColor.RESET + ChatColor.GRAY + "Accepted!");
            return true;
          }
          if (x == 2.0D)
          {
            sender.sendMessage(ChatColor.DARK_GREEN + "Current Status " + ChatColor.GRAY + "- " + ChatColor.RED + ChatColor.BOLD + "[D] " + ChatColor.RESET + ChatColor.GRAY + "Declined. Try again next build contest!");
            return true;
          }
          sender.sendMessage(ChatColor.DARK_GREEN + "Current Status " + ChatColor.GRAY + "- " + ChatColor.YELLOW + ChatColor.BOLD + "[P] " + ChatColor.RESET + ChatColor.GRAY + "Your plot is currently waiting for a result.");
          return true;
        }
        sender.sendMessage(ChatColor.RED + "You have not requested a plot review. Type /build submit to request a plot review.");
        return true;
      }
      if ((args[0].equalsIgnoreCase("support"))) // Messages an online admin for assistance
      {
    	if (!(sender.hasPermission("discoreview.support")))
    	{
    		sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
    		return true;
    	  }
        sender.sendMessage(ChatColor.GREEN + "You have successfully requested support. An online administrator has been alerted.");
        Bukkit.broadcast(ChatColor.RED + name + " has requested support. Type /tp to teleport to them.", "review.alert");
        return true;
      }
      if ((args[0].equalsIgnoreCase("cancel"))) // Cancels your submitted plot from the list
      {
    	if (!(sender.hasPermission("discoreview.review")))
    	{
    		sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
    		return true;
    	}
        getpdata().set("dr." + name, null);
        if (submits.contains(player.getName()))
        {
        	submits.remove(player.getName());
        	getpdata().set("submitted", submits);
        }     	
        savepdata();
        sender.sendMessage(ChatColor.GREEN + "You have successfully removed your plot from the review list. You still won't be able to build until the contest is over.");
        Bukkit.broadcast(ChatColor.RED + name + " has canceled their plot review request.","discoreview.alert");
        return true;
      }
      if ((args[0].equalsIgnoreCase("submit"))) // Submits the plot to be reviewed
      {
    	if (!(sender.hasPermission("discoreview.review")))
    	{
    		sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
    		return true;
    	}
    	if (!(world.contains(loc.getWorld().getName())))
    	{ 
    		sender.sendMessage(ChatColor.RED + "You must be in a build competition world to submit a build.");
    		return true;
    	}
    	if (args.length == 1)
    	{
    		sender.sendMessage(ChatColor.RED + "Are you sure? Once you submit you cannot build on your plot until the contest is over. /build submit confirm.");
    		return true;
    	}
    	if ((args[1].equalsIgnoreCase("confirm")))
    	{
    		if (allsubmits.contains(player.getName()))
    		{
    			sender.sendMessage(ChatColor.RED + "You have already submitted a build this contest.");
    			return true;
    		}
			getpdata().set("dr." + name + ".complete", null);
			getpdata().set("dr." + name + ".world", loc.getWorld().getName().toString());
			getpdata().set("dr." + name + ".x", Double.valueOf(loc.getX()));
			getpdata().set("dr." + name + ".y", Double.valueOf(loc.getY()));
			getpdata().set("dr." + name + ".z", Double.valueOf(loc.getZ()));
			getpdata().set("dr." + name + ".yaw", Float.valueOf(loc.getYaw()));
			getpdata().set("dr." + name + ".pitch", Float.valueOf(loc.getPitch()));
			submits.add(player.getName());
			allsubmits.add(player.getName());
			plotIDs.add(player.getWorld().getName() + ";" + playerplot);
			getpdata().set("plotIDs", plotIDs);
			getpdata().set("submitted", submits);
			getpdata().set("allsubmitted", allsubmits);
			savepdata();
			if (this.cfg.getString("build after submit").equalsIgnoreCase("false"))
			{	
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),"lp user " + player.getName() + " permission set worldguard.build.* false global " + loc.getWorld().getName());
				sender.sendMessage(ChatColor.GREEN + "You have successfully requested a plot review. You cannot build in this world until the build contest is over.");
				Bukkit.broadcast(ChatColor.GOLD + name + " has submitted their plot for review. Type /build goto [name] to teleport to the player.", "discoreview.alert");
				return true;
			}
			if (this.cfg.getString("build after submit").equalsIgnoreCase("true"))
			{
				sender.sendMessage(ChatColor.GREEN + "You have successfully requested a plot review.");
				Bukkit.broadcast(ChatColor.GOLD + name + " has submitted their plot for review. Type /build goto [name] to teleport to the player.", "discoreview.alert");
				return true;
			}
    	}
      }
      if (args.length == 2)
      {
        if ((args[0].equalsIgnoreCase("goto"))) // Teleports you to the players' submit location
        {
      	  if (!(sender.hasPermission("discoreview.goto")))
      	  {
      		  sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
      		  return true;
      	  }
          if (getpdata().contains("dr." + args[1] + ".x"))
          {
            World w = Bukkit.getWorld(getpdata().getString("dr." + args[1] + ".world"));
            double x = getpdata().getDouble("dr." + args[1] + ".x");
            double y = getpdata().getDouble("dr." + args[1] + ".y");
            double z = getpdata().getDouble("dr." + args[1] + ".z");
            float yaw = (float)getpdata().getDouble("dr." + args[1] + ".yaw");
            float pitch = (float)getpdata().getDouble("dr." + args[1] + ".pitch");
            Location loc1 = new Location(w, x, y, z, yaw, pitch);
            player.teleport(loc1);
            sender.sendMessage(ChatColor.GRAY + "Teleporting you to " + args[1] + "'s plot!");
            sender.sendMessage(ChatColor.GRAY + "To confirm you have been do " + ChatColor.GREEN + "/build <accept|decline> <username>");
            savepdata();
            return true;
          }
          sender.sendMessage(ChatColor.RED + args[1] + " has not requested a plot review.");
          return true;
        }
        if ((args[0].equalsIgnoreCase("decline"))) // Declines a player's submitted plot
        {     
      	    if (!(sender.hasPermission("discoreview.decline")))
      	    {
      		    sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
      		    return true;
      	    }
        	if (declines.contains(args[1]))
        	{
        		sender.sendMessage(ChatColor.RED + "That player has already had their plot reviewed");
        		return true;
        	}
        	if (accepts.contains(args[1]))
        	{
        		sender.sendMessage(ChatColor.RED + "That player has already had their plot reviewed");
        		return true;
        	}
        	if (!declines.contains(args[1]))
        	{
            	Object bless = Integer.valueOf(2);
            	getpdata().set("dr." + args[1] + ".complete", bless);
            	declines.add(args[1]);
            	getpdata().set("declined", declines);
            	savepdata();
            	sender.sendMessage(ChatColor.RED + args[1] + "'s plot has been declined.");
            	if (submits.contains(args[1]))
            	{
            		submits.remove(args[1]);
            		getpdata().set("submitted", submits);
            		savepdata();
            	}
            	return true;
        	}
        }
        if ((args[0].equalsIgnoreCase("accept"))) // Accepts a player's submitted plot
        {
      	    if (!(sender.hasPermission("discoreview.accept")))
      	    {
      		    sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command.");
      		    return true;
      	    }
        	if (accepts.contains(args[1]))
        	{
        		sender.sendMessage(ChatColor.RED + "That player has already had their plot reviewed");
        		return true;
        	}
            if (!accepts.contains(args[1]))
            {
                Object yes = Integer.valueOf(1);
                getpdata().set("dr." + args[1] + ".accept", yes);
              	accepts.add(args[1]);
                getpdata().set("accepted", accepts);
                savepdata();
            }
            if (submits.contains(args[1]))
            {
            	submits.remove(args[1]);
            	getpdata().set("submitted", submits);
            	savepdata();
            }
            savepdata();
            sender.sendMessage(ChatColor.GREEN + args[1] + "'s plot has been accepted.");

            return true;
        }
      }
    }
	return false;
  }
}


	

      

      


