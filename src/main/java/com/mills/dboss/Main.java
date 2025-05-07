package com.mills.dboss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    public static String prefix = ChatColor.translateAlternateColorCodes('&', "&c&lBOSS &8» &7");
    public static World world = Bukkit.getWorld("world");

    @Override
    public void onEnable() {

        instance = this;
        PortalManager portalManager = new PortalManager();
        Bukkit.getPluginManager().registerEvents(portalManager, this);
        Bukkit.getPluginManager().registerEvents(new SummonerSpawning(), this);
        portalManager.spawnPortals();
        SummonerSpawning.startSpawning();
        getCommand("dbossadmin").setExecutor(new AdminCommand());
    }

    public static Main getInstance() {
        return instance;
    }
}
