package com.mills.dboss;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SummonerSpawning implements Listener {

    public static ItemStack eyeItem() {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Summoning Eye");
        item.setItemMeta(meta);
        return item;
    }

    private static int MOB_COUNT = 0;

    public static void startSpawning() {
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld("world");
                Location center = new Location(world, 0, 0, 0);

                int amountNeeded = Math.max(0, 25 - getMobCount());

                for (int i = 0; i <= amountNeeded; i++) {
                    Location loc = getRandomLocation(center, 500);
                    spawnSummoner(loc);
                }

            }
        }.runTaskTimer(Main.getInstance(), 0L, 100L);
    }

    private static NamespacedKey key = new NamespacedKey(Main.getInstance(), "summoner");

    private static void spawnSummoner(Location loc) {
        Enderman enderman = (Enderman) loc.getWorld().spawnEntity(loc, EntityType.ENDERMAN);

        enderman.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
        enderman.getAttribute(Attribute.MAX_HEALTH).setBaseValue(300.0);
        enderman.setHealth(300.0);
        double health = enderman.getHealth();
        enderman.setCustomName(ChatColor.DARK_PURPLE + "Summoner " + ChatColor.RED + "(" + health + "/100)");
        enderman.setCustomNameVisible(true);

        int mob = getMobCount();
        setMobCount(mob + 1);

    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Enderman)) return;

        Enderman enderman = (Enderman) e.getEntity();
        if (enderman.getKiller() == null) return;

        int chance = 5;

        if (enderman.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            e.setDroppedExp(0);
            e.getDrops().clear();
            if (ThreadLocalRandom.current().nextInt(100) < chance) {
                Player player = e.getEntity().getKiller();
                player.getInventory().addItem(eyeItem());
                player.sendMessage(Main.prefix + "You have recieved a summoning eye!");
            }

            setMobCount(Math.max(0, getMobCount() - 1));
        }
    }

    private static Location getRandomLocation(Location center, int radius) {
        Random random = new Random();

        World world = center.getWorld();

        double angle = 2 * Math.PI * random.nextDouble();
        double distance = radius * Math.sqrt(random.nextDouble());

        double x = center.getX() + distance * Math.cos(angle);
        double z = center.getZ() + distance * Math.sin(angle);
        double y = world.getHighestBlockYAt((int) x, (int) z) + 1;

        return new Location(world, x, y, z);
    }

    private static Integer getMobCount() {
        return MOB_COUNT;
    }

    private static void setMobCount(Integer mobCount) {
        MOB_COUNT = mobCount;
    }
}
