package com.noyhillel.survivalgames.arena.setup;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.Arena;
import com.noyhillel.survivalgames.arena.ArenaException;
import com.noyhillel.survivalgames.arena.ArenaMeta;
import com.noyhillel.survivalgames.arena.Point;
import com.noyhillel.survivalgames.player.GPlayer;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Data
public final class ArenaSetup implements SetupSession {
    private final GPlayer player;
    private final World world;
    private Point l1 = null; //Regions for chest detection
    private Point l2 = null;

    private List<Point> tier1 = new ArrayList<>();
    private List<Point> tier2 = new ArrayList<>();
    private List<Point> cornicopiaSpawns = new ArrayList<>();
    public static ArenaMeta arenaMeta = new ArenaMeta("Untitled", Arrays.asList("Joey", "Noy"), "Ask an admin for this information.");

    public ArenaSetup(GPlayer player, World world) {
        this.player = player;
        this.world = world;
        Bukkit.getPluginManager().registerEvents(this, SurvivalGames.getInstance());
    }

    @Override
    public void start() {
        Player p = player.getPlayer();
        player.resetPlayer();
        p.setAllowFlight(true);
        p.setGameMode(GameMode.CREATIVE);
        player.playSound(Sound.LEVEL_UP);
        p.getInventory().addItem(stackWithName(Material.DIAMOND_SPADE, ChatColor.RED + "Tier one selector")); // Tier 1
        p.getInventory().addItem(stackWithName(Material.IRON_SPADE, ChatColor.RED + "Tier two selector")); // Tier 2
        p.getInventory().addItem(stackWithName(Material.GOLD_SPADE, ChatColor.RED + "Cornicopia spawn selector")); // Corn
        p.getInventory().addItem(stackWithName(Material.DIAMOND_AXE, ChatColor.RED + "Region selector")); //region selector
    }

    static ItemStack stackWithName(Material m, String name) {
        ItemStack itemStack = new ItemStack(m);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void commit() throws ArenaException {
        if (l1 == null
                || l2 == null
                || tier1.size() == 0
                || tier2.size() == 0
                || cornicopiaSpawns.size() == 0
                || arenaMeta == null)
            throw new ArenaException(null, null, "You must setup all parts of the arena!");
        Arena arena = new Arena(cornicopiaSpawns, tier1, tier2, arenaMeta, world);
        SurvivalGames.getInstance().getArenaManager().saveArena(arena);

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player1 = event.getPlayer();
        if (player1.getItemInHand() == null) return;
        if (player1.getItemInHand().getItemMeta() == null) return;
        if (!player1.getName().equals(player.getPlayer().getName())) return;
        if (action == Action.PHYSICAL) return;
        Point actualLocation = Point.of(player1.getLocation());
        Point clickedBlock = action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK ? Point.of(event.getClickedBlock()) : null;
        switch (event.getItem().getType()) {
            case DIAMOND_SPADE:
                if (clickedBlock == null) return;
                tier1.add(clickedBlock);
                player.sendMessage(ChatColor.GREEN + "Point for tier 1 selected");
                break;
            case IRON_SPADE:
                if (clickedBlock == null) return;
                player.sendMessage(ChatColor.DARK_GREEN + "Point for tier 2 selected");
                tier2.add(clickedBlock);
                break;
            case GOLD_SPADE:
                cornicopiaSpawns.add(actualLocation);
                player.sendMessage(ChatColor.DARK_AQUA + "Spawn point selected.");
                break;
            case DIAMOND_AXE:
                if (!(action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK)) return;
                if (action == Action.RIGHT_CLICK_BLOCK) {
                    l1 = clickedBlock;
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Region Point 2 selected.");
                    return;
                } else if (action == Action.LEFT_CLICK_BLOCK) {
                    l2 = clickedBlock;
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Region Point 1 selected.");
                    return;
                }
                break;
            default:
                return;
        }
        event.setCancelled(true);
    }

    public void linkChests(LinkedList<Block> blockList) {
        for (Block block : blockList) {
            if (block.getType() == Material.SKULL) {
                switch (((Skull) block.getState()).getSkullType()) {
                    case ZOMBIE:
                        tier1.add(Point.of(block));
                        block.setType(Material.CHEST);
                        break;
                    case CREEPER:
                        tier2.add(Point.of(block));
                        block.setType(Material.CHEST);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}