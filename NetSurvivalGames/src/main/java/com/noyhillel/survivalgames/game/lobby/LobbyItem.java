package com.noyhillel.survivalgames.game.lobby;

import com.noyhillel.survivalgames.game.GameManager;
import com.noyhillel.survivalgames.game.impl.SGGame;
import com.noyhillel.survivalgames.player.SGPlayer;
import com.noyhillel.survivalgames.utils.MessageManager;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/*
 Slots:
 [1] [2] [3] [4] [5] [6] [7] [8] [9]

 Anything outside this indice will throw an IllegalArgumentException
 */
public enum LobbyItem {
    PREGAME_BOOK(new LobbyItemDefinition() {
        @Override
        Integer getSlot() {
            return 9;
        }

        @Override
        Material getType() {
            return Material.WRITTEN_BOOK;
        }

        @Override
        String getTitle() {
            return MessageManager.getFormat("pregame-book.title", false);
        }

        @Override
        void leftClick(SGPlayer player, GameManager manager) { rightClick(player, manager); }

        @Override
        void rightClick(SGPlayer player, GameManager manager) {}

        @Override
        protected ItemStack getItem(SGPlayer player, GameManager manager) {
            ItemStack book = new ItemStack(getType());
            BookMeta meta = (BookMeta) book.getItemMeta();
            meta.setTitle(getTitle());
            meta.setPages(getPlugin().getConfig().getStringList("pregame-book.pages"));
            meta.setAuthor(MessageManager.getFormat("pregame-book.author", false));
            book.setItemMeta(meta);
            return book;
        }
    }),
    SPECTATOR_COMPASS(new LobbyItemDefinition() {
        @Override
        Integer getSlot() {
            return 1;
        }

        @Override
        Material getType() {
            return Material.COMPASS;
        }

        @Override
        String getTitle() {
            return MessageManager.getFormat("formats.spectator-compass-title", false);
        }

        @Override
        public void rightClick(SGPlayer player, GameManager manager) {
            SGGame runningSGGame = manager.getRunningSGGame();
            if (runningSGGame == null) return;
            runningSGGame.getSpectatorGUI().open(player);
        }
        @Override
        public void leftClick(SGPlayer player, GameManager manager) {
            rightClick(player, manager);
        }

    });

    @Getter private final LobbyItemDefinition lobbyItemDefinition;
    LobbyItem(LobbyItemDefinition definition) {
        this.lobbyItemDefinition = definition;
    }
}
