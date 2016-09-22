package com.noyhillel.survivalgames.command;

import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.game.arena.Point;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.setup.ArenaSetup;
import com.noyhillel.survivalgames.arena.setup.SetupSession;
import com.noyhillel.survivalgames.player.SGPlayer;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.noyhillel.survivalgames.command.SetupCommand.setupSessions;

@Permission("survivalgames.linkchests")
@CommandMeta(name = "linkchests", description = "The Link Chests Command", usage = "/linkchests")
public final class LinkChestsCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (!SurvivalGames.getInstance().isSetupOnly()) throw new NewNetCommandException("The server needs to be in setup mode!", NewNetCommandException.ErrorType.SPECIAL);
        SGPlayer sgPlayer = resolveGPlayer(sender);
        if (!setupSessions.containsKey(sgPlayer)) throw new NewNetCommandException("You are not currently setting up an arena!", NewNetCommandException.ErrorType.SPECIAL);
        SetupSession sSes = setupSessions.get(sgPlayer);
        if (!(sSes instanceof ArenaSetup)) throw new NewNetCommandException("You are not currently setting up an Arena!", NewNetCommandException.ErrorType.SPECIAL);
        ArenaSetup arenaSetup = (ArenaSetup) sSes;
        if (args.length < 1) throw new NewNetCommandException("You did not specify enough arguments!", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        String arg = args[0];
        if (!(arg.equalsIgnoreCase("tier1") || arg.equalsIgnoreCase("tier2"))) throw new NewNetCommandException("You did not specify a valid argument, needs to be tier1 or tier2!", NewNetCommandException.ErrorType.SPECIAL);
        Point l1 = arenaSetup.getL1();
        Point l2 = arenaSetup.getL2();
        if (l1 == null || l2 == null) throw new NewNetCommandException("You need to specify a region!", NewNetCommandException.ErrorType.NULL);
        Double maxX = Math.max(l1.getX(), l2.getX());
        Double maxY = Math.max(l1.getY(), l2.getY());
        Double maxZ = Math.max(l1.getZ(), l2.getZ());
        Double minX = Math.min(l1.getX(), l2.getX());
        Double minY = Math.min(l1.getY(), l2.getY());
        Double minZ = Math.min(l1.getZ(), l2.getZ());
        SkullType skullType = arg.equalsIgnoreCase("tier1") ? SkullType.ZOMBIE : SkullType.CREEPER; //Since we can safely assume that tier2 is the arg if it's not tier1
        sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "The server may crash! We're about to iterate through a lot of blocks!");
        List<Point> pointList = new ArrayList<>();
        for (int x = minX.intValue(); x <= maxX; x++) {
            for (int y = minY.intValue(); y <= maxY; y++) {
                for (int z = minZ.intValue(); z <= maxZ; z++) {
                    Block blockAt = arenaSetup.getWorld().getBlockAt(x, y, z);
                    if (blockAt.getType() != Material.SKULL) continue;
                    Skull state = (Skull) blockAt.getState();
                    if (state.getSkullType() != skullType) continue;
                    pointList.add(Point.of(blockAt));
                    blockAt.setType(Material.AIR);
                }
            }
        }
        sender.sendMessage(ChatColor.GREEN + "It's over! Chests linked = " + pointList.size());
        if (arg.equalsIgnoreCase("tier1")) arenaSetup.setTier1(pointList);
        else if (arg.equalsIgnoreCase("tier2")) arenaSetup.setTier2(pointList);
        else
            throw new NewNetCommandException("Lol found all the chests but can't link them!", NewNetCommandException.ErrorType.SPECIAL);
    }

    @SneakyThrows
    public static SGPlayer resolveGPlayer(Player player) {
        if (player == null) throw new NewNetCommandException("Player not found!", NewNetCommandException.ErrorType.NULL);
        return SurvivalGames.getInstance().getSgPlayerManager().getOnlinePlayer(player);
    }
}