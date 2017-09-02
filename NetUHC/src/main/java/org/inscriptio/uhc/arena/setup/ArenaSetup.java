package org.inscriptio.uhc.arena.setup;

import com.noyhillel.networkengine.exceptions.ArenaException;
import com.noyhillel.networkengine.game.arena.ArenaMeta;
import com.noyhillel.networkengine.game.arena.Point;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.arena.Arena;
import org.inscriptio.uhc.command.SetupCommand;
import org.inscriptio.uhc.player.UHCPlayer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
@Data
public final class ArenaSetup implements SetupSession {
    private final UHCPlayer player;
    private final World world;
    private Point l1 = null; //Regions for chest detection
    private Point l2 = null;

    private List<Point> tier1 = new ArrayList<>();
    private List<Point> tier2 = new ArrayList<>();
    private List<Point> randomSpotSpawns = new ArrayList<>();
    public static ArenaMeta arenaMeta = new ArenaMeta(SetupCommand.name, SetupCommand.authors, SetupCommand.socialLink);

    public ArenaSetup(UHCPlayer player, World world) {
        this.player = player;
        this.world = world;
        Bukkit.getPluginManager().registerEvents(this, NetUHC.getInstance());
    }

    @Override
    public void start() {
        Player p = player.getPlayer();
        player.resetPlayer();
        p.setAllowFlight(true);
        p.setGameMode(GameMode.CREATIVE);
        player.playSound(Sound.ENTITY_PLAYER_LEVELUP);
        p.getInventory().addItem(itemStackWithName(Material.GOLD_SPADE, ChatColor.RED + "Cornicopia spawn selector")); // Corn
        p.getInventory().addItem(itemStackWithName(Material.DIAMOND_AXE, ChatColor.RED + "Region selector")); //region selector
        /*Deprecated*/
        //p.getInventory().addItem(itemStackWithName(Material.DIAMOND_SPADE, ChatColor.RED + "Tier one selector")); // Tier 1
        //p.getInventory().addItem(itemStackWithName(Material.IRON_SPADE, ChatColor.RED + "Tier two selector")); // Tier 2 | Leaving this out for now
    }

    static ItemStack itemStackWithName(Material m, String name) {
        ItemStack stack = new ItemStack(m);
        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.setDisplayName(name);
        stack.setItemMeta(itemMeta);
        return stack;
    }

    @Override
    public void commit() throws ArenaException {
        if (l1 == null
                || l2 == null
                || tier1.size() == 0
                || tier2.size() == 0
                || randomSpotSpawns.size() == 0
                || arenaMeta == null)
            throw new ArenaException(null, null, "You must setup all parts of the arena!");
        Arena arena = new Arena(randomSpotSpawns, tier1, tier2, arenaMeta, world);
        NetUHC.getInstance().getArenaManager().saveArena(arena);

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
                randomSpotSpawns.add(actualLocation);
                player.sendMessage(ChatColor.DARK_AQUA + "Spawn point selected.");
                break;
            case DIAMOND_AXE:
                if (!(action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK)) return;
                if (action == Action.RIGHT_CLICK_BLOCK) {
                    l1 = clickedBlock;
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Region Point 2 selected."); //world edit lmfao
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
}