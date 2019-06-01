package me.drunkenmeows.mobmoney;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MobMoney extends JavaPlugin {
	
	//public static List<Integer> ExcludeMobs = new ArrayList<Integer>();
	public HashMap<String,playerInfo> players = new HashMap<String,playerInfo>();
	
	private static MobMoney plugin;
	public final String msgPrefix = "&f[&aMobMoney&f] ";
	
	public static Economy econ = null;
	public Config config = new Config();
	//default state
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public Metrics metrics;
	
	@Override
	public void onDisable()	{
		//output to console
		HandlerList.unregisterAll(this);
	}	
	
	@Override
	public void onEnable()	{
		if (!setupEconomy() ) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		//instanitate plugin
		plugin = this;
				
		//if there a server reload populate the player hash again.
		for(Player p: getServer().getOnlinePlayers()) {
			plugin.players.put(p.getName(), new playerInfo());
		}

		//save config.yml into plugin folder
		this.saveDefaultConfig();
		
		config.loadsettings(this);
				
		//listen for events	
		getServer().getPluginManager().registerEvents(new MoneyListener(plugin), plugin);
		
		//start metrics
		try {
		    this.metrics = new Metrics(this);
		    //huntcount = metrics.createGraph("Hunt Played");
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) {
            logger.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }

       /*Player player = (Player)sender;

       if(command.getLabel().equals("test-economy")) {
            // Lets give the player 1.05 currency (note that SOME economic plugins require rounding!)
            sender.sendMessage(String.format("You have %s", econ.format(econ.getBalance(player.getName()))));
            EconomyResponse r = econ.depositPlayer(player.getName(), 1.05);
            if(r.transactionSuccess()) {
                sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
                return true;
            } else {
                sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
                return true;
            }
        }*/
		return false;
	}
	
	public String colourise(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
}
