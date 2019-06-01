package me.drunkenmeows.mobmoney;

public class playerInfo {

	public String lastkilled;
	public float multiplier;
	
	public playerInfo()	{
		lastkilled = "";
		multiplier = 1;
	}
	
	public playerInfo(float mul, String mob) {
		lastkilled = mob;
		multiplier = mul;
	}
}
