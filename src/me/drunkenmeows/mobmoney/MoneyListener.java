package me.drunkenmeows.mobmoney;

import java.util.Random;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class MoneyListener implements Listener {
	private final MobMoney plugin;
	
	//constructor
	public MoneyListener(MobMoney plugin)	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerjoin(PlayerJoinEvent event) {
		plugin.players.put(event.getPlayer().getName(), new playerInfo());		
	}
	

	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent spawnevent){
		
		boolean worldallowed = false;
		
		for(String world : plugin.config.worlds) {
			if(spawnevent.getEntity().getWorld().getName().equals(world))
				worldallowed = true;
		}
		
		if(!worldallowed)
			return;
		
		if(plugin.config.denyMobSpawners)	{	
			if ((spawnevent.getSpawnReason().toString() == "SPAWNER") || (spawnevent.getSpawnReason().toString() == "SPAWNER_EGG"))	{
				spawnevent.getEntity().setMetadata("mmSpawnered", new FixedMetadataValue(this.plugin, true));
				return;
			}
		}
		
		
		if(plugin.config.denyNaturalGrinders && (spawnevent.getEntity().getWorld().getEnvironment() != World.Environment.NETHER)) {
			//check if spawned in mob grinder
			Block block = spawnevent.getLocation().getBlock();
			int vertdist = 255 - block.getY();			
			for(int i = 1; i < vertdist; i++) {
				block = block.getRelative(BlockFace.UP);
				//if spawned under block that are not safe.
				if(!block.isEmpty() && (block.getType() != Material.LEAVES) && (block.getType() != Material.LOG))	{
						spawnevent.getEntity().setMetadata("mmSpawnered", new FixedMetadataValue(this.plugin, true));
						break;
				}
			}
		}
		
		return;
	}
	

	@EventHandler
	public void onMobDeath(EntityDeathEvent event)
	{
		LivingEntity lm = event.getEntity();
		Player p = lm.getKiller();

		//plugin.getServer().broadcastMessage(plugin.colourise("players:"+plugin.players.size()));		
		if(p == null)
			return;
		
		boolean worldallowed = false;
		
		for(String world : plugin.config.worlds)
		{
			//plugin.getServer().broadcastMessage(plugin.colourise("Worlds:"+world));
			if(p.getWorld().getName().equals(world))
				worldallowed = true;
		}
		
		if(!worldallowed) {
			//plugin.getServer().broadcastMessage(plugin.colourise("world:"+p.getWorld()+" not allowed"));
			return;	
		}
		
		if(lm.hasMetadata("mmSpawnered")) {
			//p.sendMessage("Spawnermob!");
		}
			
		//float money = 0;
		String mob = "Unknown";
		
		mob = lm.getType().getName();
		
		if(lm instanceof Player)
			mob = "Player";
		
		//p.sendMessage("Mob:"+mob);
		
		if(mob.equalsIgnoreCase("skeleton"))
		{
			if( ( (Skeleton)lm ).getSkeletonType().getId() == 1 ) {
				mob = "WitherSkeleton";
			}
		}
		
		String mobkey = "Value.".concat(mob);
		
		float money = 0;
		String strmoney = "";

		if(plugin.getConfig().contains(mobkey)) {
			//money = plugin.getConfig().getInt(mobkey);
		
			strmoney = plugin.getConfig().getString(mobkey);
			
			if(strmoney.contains(":"))
			{
				String[] minmax = strmoney.split(":");
				float min = Float.parseFloat(minmax[0]);
				float max = Float.parseFloat(minmax[1]);
				money = Math.round(new Random().nextFloat() * (max - min) + min);
				//int x = new Random().nextInt((config.Xmax-1)-(config.Xmin+1))+config.Xmin+1;
			} else {
				money = Float.parseFloat(strmoney);
			}
		} else {
			//mob not in config.
			if(p.isOp()) {
				p.sendMessage(plugin.colourise(plugin.msgPrefix+"&cMob &f[&a"+mob+"&f]&c not defined in config."));
			}
			return;	
		}
		
		//multiplier for decreasing value
		float mul = 1;
		if(( plugin.players.get(p.getName()).lastkilled.equalsIgnoreCase(mob) && (money > 0) ) || lm.hasMetadata("mmSpawnered") ) {
			mul = plugin.players.get(p.getName()).multiplier;
			mul = mul - (float)plugin.getConfig().getDouble("Multiplier");
			plugin.players.put(p.getName(), new playerInfo(mul,mob));
		}	else	{
			plugin.players.put(p.getName(), new playerInfo(mul,mob));
		}
		
		
		/*if(( plugin.players.get(p.getName()).lastkilled.equalsIgnoreCase(mob) && (money > 0) ) || lm.hasMetadata("mmSpawnered") ) {
			mul = plugin.players.get(p.getName()).multiplier;
			mul = mul - (float)plugin.getConfig().getDouble("Multiplier");
			plugin.players.put(p.getName(), new playerInfo(mul,mob));
		}	else	{
			plugin.players.put(p.getName(), new playerInfo(mul,mob));
		}*/
		
		//cap mul to 0
		if(mul < 0)
			mul = 0;
		
		//final value
		money = money * mul;
		
		if(money != 0.0) {
			if(money < 0) {
				EconomyResponse d = MobMoney.econ.withdrawPlayer(p.getName(), Math.abs(money));
				if(d.transactionSuccess())
		        	p.sendMessage(plugin.colourise(this.plugin.msgPrefix+"&cYou have killed a &f[&a"+mob+"&f]&c for &f[&a$"+money+"&f]"));
				
			} else if(money > 0) {
				EconomyResponse r = MobMoney.econ.depositPlayer(p.getName(), money);
		        if(r.transactionSuccess())
		        	p.sendMessage(plugin.colourise(this.plugin.msgPrefix+"&cYou have killed a &f[&a"+mob+"&f]&c for &f[&a$"+money+"&f]"));
		       	}
		}
	}
}
