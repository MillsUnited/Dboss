package com.mills.dboss;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PortalManager implements Listener {

    private final NamespacedKey hologramKey = new NamespacedKey(Main.getInstance(), "portal_hologram");

    private final Map<Location, Integer> portalEyes = new HashMap<>();
    private final Set<Location> portalBlocks = new HashSet<>();

    private final List<BlockPlacement> portalPlacements = Arrays.asList(
            new BlockPlacement(100, 64, 203, BlockFace.WEST),
            new BlockPlacement(100, 64, 199, BlockFace.WEST),
            new BlockPlacement(102, 64, 197, BlockFace.NORTH),
            new BlockPlacement(106, 64, 197, BlockFace.NORTH),
            new BlockPlacement(108, 64, 199, BlockFace.EAST),
            new BlockPlacement(108, 64, 203, BlockFace.EAST),
            new BlockPlacement(106, 64, 205, BlockFace.SOUTH),
            new BlockPlacement(102, 64, 205, BlockFace.SOUTH)
    );

    public void spawnPortals() {
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        for (BlockPlacement placement : portalPlacements) {
            Location loc = new Location(world, placement.x, placement.y, placement.z);
            world.getChunkAt(loc).load();
            Block block = loc.getBlock();
            block.setType(Material.END_PORTAL_FRAME);

            if (block.getBlockData() instanceof Directional directional) {
                directional.setFacing(placement.face);
                block.setBlockData(directional, false);
            }

            portalBlocks.add(loc);
            portalEyes.put(loc, 0);
            spawnOrUpdateHologram(block, 0);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if (block == null || !isPortalBlock(block)) return;

            ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            e.setCancelled(true);
            if (!item.isSimilar(SummonerSpawning.eyeItem())) return;

            int eyes = getPortalEyes(block);
            if (eyes < 2) {

                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    e.getPlayer().getInventory().setItemInMainHand(null);
                }

                eyes++;
                setPortalEyes(block, eyes);

                if (block.getBlockData() instanceof EndPortalFrame frame) {
                    frame.setEye(eyes == 2);
                    block.setBlockData(frame, false);
                }

                spawnOrUpdateHologram(block, eyes);

                int currentPlacedEyes = getTotalEyesPlaced();

                for (Player player : Bukkit.getWorld("world").getPlayers()) {
                    player.sendMessage(Main.prefix + player.getName() + " has placed eye " + currentPlacedEyes + "/16!");
                }

                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);

                if (eyes == 2) {
                    // method for summon dragon
                }

                if (areAllPortalsComplete()) {
                    for (Player player : Bukkit.getWorld("world").getPlayers()) {
                        player.sendMessage(Main.prefix + "boss is spawning!");
                    }
                }
            }
        } else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            Block block = e.getClickedBlock();
            if (block != null && isPortalBlock(block)) {
                e.setCancelled(true);
            } else if (e.getPlayer().getInventory().getItemInMainHand().isSimilar(SummonerSpawning.eyeItem())) {
                e.setCancelled(true);
            }
        }
    }

    private boolean areAllPortalsComplete() {
        return portalEyes.values().stream().allMatch(eyes -> eyes == 2);
    }

    public int getTotalEyesPlaced() {
        return portalEyes.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void resetAllPortalEyes() {
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        for (BlockPlacement placement : portalPlacements) {
            Location loc = new Location(world, placement.x, placement.y, placement.z);
            Block block = loc.getBlock();

            if (block.getBlockData() instanceof EndPortalFrame frame) {
                frame.setEye(false);
                block.setBlockData(frame, false);
            }

            portalEyes.put(loc, 0);
            spawnOrUpdateHologram(block, 0);
        }
    }

    private boolean isPortalBlock(Block block) {
        return portalBlocks.contains(block.getLocation());
    }

    private int getPortalEyes(Block block) {
        return portalEyes.getOrDefault(block.getLocation(), 0);
    }

    private void setPortalEyes(Block block, int count) {
        portalEyes.put(block.getLocation(), count);
    }

    private void spawnOrUpdateHologram(Block block, int eyes) {
        World world = block.getWorld();
        Location holoLoc = block.getLocation().add(0.5, 1.0, 0.5);

        world.getChunkAt(holoLoc).load();

        String displayText = ChatColor.RED + (eyes == 2 ? "Lit" : eyes + "/2");

        ArmorStand existing = null;
        for (Entity entity : world.getNearbyEntities(holoLoc, 0.75, 0.75, 0.75)) {
            if (entity instanceof ArmorStand armorStand && armorStand.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE)) {
                existing = armorStand;
                break;
            }
        }

        if (existing == null) {
            ArmorStand stand = world.spawn(holoLoc, ArmorStand.class);
            stand.setInvisible(true);
            stand.setCustomNameVisible(true);
            stand.setCustomName(displayText);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setMarker(true);
            stand.setSmall(true);
            stand.getPersistentDataContainer().set(hologramKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            existing.setCustomName(displayText);
        }
    }
}
