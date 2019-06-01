package me.drunkenmeows.mobmoney;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.PluginDescriptionFile;

public class Config {

	public List<String> worlds = new ArrayList<String>();
	public boolean denyMobSpawners;
	public boolean denyNaturalGrinders;
	public double multiplier;
	
	public Config() {
	}
	
	public void loadsettings(MobMoney p) {
		PluginDescriptionFile pdfFile = p.getDescription();
		this.worlds = p.getConfig().getStringList("Worlds");
		String money = p.getConfig().getString("money");
		double dmoney = Double.valueOf(money);
		
		p.logger.info("["+p.getConfig().getName()+"] Smoney["+money+"] | DMoney["+dmoney+"]" );
		
		p.logger.info("["+pdfFile.getName()+"] using worlds [ "+this.worlds.toString()+"]" );
		
		this.denyMobSpawners = p.getConfig().getBoolean("denyMobSpawners", true);
		this.denyNaturalGrinders = p.getConfig().getBoolean("denyNaturalGrinders", true);
		this.multiplier = p.getConfig().getDouble("Multiplier", 0.25);
	}
	
}