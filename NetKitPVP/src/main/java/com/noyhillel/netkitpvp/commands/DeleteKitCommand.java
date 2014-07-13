package com.noyhillel.netkitpvp.commands;

import com.noyhillel.netkitpvp.MessageManager;
import com.noyhillel.netkitpvp.NetKitPVP;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * <p/>
 * Latest Change: 12/07/2014.
 * <p/>
 *
 * @author Noy
 * @since 12/07/2014.
 */
@CommandMeta(name = "deletekit", description = "The Kit Command", usage = "/kit")
@Permission("kitpvp.admin")
public final class DeleteKitCommand extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player p, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException("Too few arguments!", NewNetCommandException.ErrorType.FewArguments);
        if (args.length > 2) throw new NewNetCommandException("Too many arguments!", NewNetCommandException.ErrorType.ManyArguments);
        ConfigurationSection kit = NetKitPVP.getInstance().getConfig().getConfigurationSection("kit.items." + args[0]);
        if (kit == null)  throw new NewNetCommandException("Invalid kit!", NewNetCommandException.ErrorType.Null);
        NetKitPVP.getInstance().getConfig().set("kit.items." + args[0], null);
        NetKitPVP.getInstance().saveConfig();
        p.sendMessage(MessageManager.getFormats("formats.delete-kit"));
    }
}
