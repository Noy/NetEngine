package com.noyhillel.netkitpvp.commands;

import com.noyhillel.netkitpvp.MessageManager;
import com.noyhillel.netkitpvp.NetKitPVP;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p/>
 * Latest Change: 12/07/2014.
 * <p/>
 *
 * @author Noy
 * @since 12/07/2014.
 */
@CommandMeta(name = "setkit", description = "The Kit Command", usage = "/kit")
@Permission("kitpvp.admin")
public final class SetKitCommand extends NetAbstractCommandHandler {


    @Override
    protected void playerCommand(Player p, String[] args) throws NewNetCommandException {
        if (args.length < 3) throw new NewNetCommandException("Too few arguments!", NewNetCommandException.ErrorType.FewArguments);
        Material m;
        try {
            m = Material.getMaterial(args[0].toUpperCase());
            if (m == null) throw new NewNetCommandException("Too few arguments!", NewNetCommandException.ErrorType.Null);
        } catch (Exception e) {
            p.sendMessage(e.getCause().toString());
            return;
        }
        String name = ChatColor.translateAlternateColorCodes('&', args[1]);
        ArrayList<String> lore = new ArrayList<>();
        lore.addAll(Arrays.asList(args).subList(2, args.length));
        ConfigurationSection section = NetKitPVP.getInstance().getConfig().createSection("kit.items." + ChatColor.stripColor(name).toLowerCase());
        section.set("item", m.name());
        section.set("name", args[1]);
        section.set("lore", lore);
        NetKitPVP.getInstance().getConfig().set("kit.items." + ChatColor.stripColor(name).toLowerCase(), section);
        NetKitPVP.getInstance().saveConfig();
        p.sendMessage(MessageManager.getFormats("formats.kit-set"));
    }
}
