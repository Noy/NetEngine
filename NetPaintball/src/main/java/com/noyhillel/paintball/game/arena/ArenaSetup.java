package com.noyhillel.paintball.game.arena;

import com.noyhillel.networkengine.exceptions.ArenaException;
import com.noyhillel.networkengine.game.arena.ArenaMeta;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.paintball.Paintball;
import com.noyhillel.paintball.command.SetupCommand;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


@Data
public final class ArenaSetup implements SetupSession {
    private final NetPlayer player;
    private final World world;

    private Location redTeamSpawn;
    private Location blueTeamSpawn;
    public static ArenaMeta arenaMeta = new ArenaMeta(SetupCommand.name, SetupCommand.authors, SetupCommand.socialLink);

    public ArenaSetup(NetPlayer player, World world) {
        this.player = player;
        this.world = world;
        Bukkit.getPluginManager().registerEvents(this, Paintball.getInstance());
    }

    @Override
    public void start() {
        Player p = player.getPlayer();
        player.resetPlayer();
        p.setAllowFlight(true);
        player.playSound(Sound.ENTITY_PLAYER_LEVELUP);
        p.getInventory().addItem(stackWithName(Material.GOLD_SPADE, ChatColor.RED + "Red Team spawn selector")); // Corn
        p.getInventory().addItem(stackWithName(Material.IRON_SPADE, ChatColor.AQUA + "Blue Team spawn selector")); //region selector
    }

    private static ItemStack stackWithName(Material m, String name) {
        ItemStack itemStack = new ItemStack(m);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void commit() throws ArenaException {
        if (redTeamSpawn == null || blueTeamSpawn == null || arenaMeta == null)
            throw new ArenaException(null, null, "You must setup all parts of the arena!");
        Arena arena = new Arena(redTeamSpawn, blueTeamSpawn, arenaMeta, world);
        Paintball.getInstance().getArenaManager().saveArena(arena);

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player1 = event.getPlayer();
        if (player1.getItemInHand() == null) return;
        if (player1.getItemInHand().getItemMeta() == null) return;
        if (!player1.getName().equals(player.getPlayer().getName())) return;
        if (action == Action.PHYSICAL) return;
        Location actualLocation = player1.getLocation();
        switch (event.getItem().getType()) {
            case IRON_SPADE:
                player.sendMessage(ChatColor.AQUA + "Point for blue team selected");
                blueTeamSpawn.add(actualLocation);
                break;
            case GOLD_SPADE:
                redTeamSpawn.add(actualLocation);
                player.sendMessage(ChatColor.RED + "Spawn point for red team selected.");
                break;
            default:
                return;
        }
        event.setCancelled(true);
    }
}
