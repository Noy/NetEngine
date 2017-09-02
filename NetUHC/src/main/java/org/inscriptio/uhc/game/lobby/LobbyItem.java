package org.inscriptio.uhc.game.lobby;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.inscriptio.uhc.game.GameManager;
import org.inscriptio.uhc.game.impl.UHCGame;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.utils.MessageManager;

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
        void leftClick(UHCPlayer player, GameManager manager) { rightClick(player, manager); }

        @Override
        void rightClick(UHCPlayer player, GameManager manager) {}

        @Override
        protected ItemStack getItem(UHCPlayer player, GameManager manager) {
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
        public void rightClick(UHCPlayer player, GameManager manager) {
            UHCGame runningUHCGame = manager.getRunningUHCGame();
            if (runningUHCGame == null) return;
            runningUHCGame.getSpectatorGUI().open(player);
        }
        @Override
        public void leftClick(UHCPlayer player, GameManager manager) {
            rightClick(player, manager);
        }

    });

    @Getter private final LobbyItemDefinition lobbyItemDefinition;
    LobbyItem(LobbyItemDefinition definition) {
        this.lobbyItemDefinition = definition;
    }
}
