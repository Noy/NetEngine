package com.noyhillel.networkhub;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Noy on 30/05/2014.
 */
public final class Announcer implements Runnable {

    /**
     * The List of messages that the announcer will announce.
     */
    private List<String> messages = new ArrayList<>();
    /**
     * The last message, default to 0.
     */
    private Integer lastMsg = 0;
    /**
     * Whether the announcements should be activated or not.
     */
    private boolean running = false;

    /**
     * Creating our constructor
     * @param hub Main class.
     */
    public Announcer(NetHub hub) {
        this.messages = hub.getConfig().getStringList("announcer.announcements");
        this.running = hub.getConfig().getBoolean("announcer.running");
    }

    /**
     * Overridden method from the Runnable interface.
     */
    @Override
    public void run() {
        // If the announcements are set to true
        if (running) {
            // Loop through all players
            for (Player p : NetHub.getInstance().getServer().getOnlinePlayers()) {
                // Send them the announcements.
                p.sendMessage(MessageManager.getFormat("formats.prefix", false) + getNextMsg());
            }

        }
    }

    /**
     * This is so that the messages can be ran in order
     * @return The messages in color.
     */
    private String getNextMsg() {
        String msg = messages.get(lastMsg);
        // If the last message is smaller than all the messages minus 1
        if (lastMsg < messages.size()-1) {
            // Message increments by one
            lastMsg++;
        } else {
            // If not, messages default back to 0.
            lastMsg = 0;
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}