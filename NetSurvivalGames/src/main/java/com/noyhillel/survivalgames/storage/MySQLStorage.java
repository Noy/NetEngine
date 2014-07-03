package com.noyhillel.survivalgames.storage;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.noyhillel.survivalgames.player.GOfflinePlayer;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.player.PlayerNotFoundException;
import com.noyhillel.survivalgames.player.StorageError;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@GStorageKey({"mysql", "sql"})
@RequiredArgsConstructor
public final class MySQLStorage implements GStorage {
    private static enum DatabaseKeys {
        PLAYERS_TABLE("players"),
        UUID("id"),
        USERNAMES("usernames"),
        POINTS("points"),
        KILLS("kills"),
        DEATHS("deaths"),
        WINS("wins"),
        TOTAL_GAMES("totalgames"),
        MUTATION_CREDITS("mutationcredits"),
        NICK("nick");
        private String key;
        DatabaseKeys(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return this.key;
        }

        public String getKey() {
            return this.key;
        }
    }

    private final String host;
    private final Integer port;
    private final String database;
    private final String username;
    private final String password;

    private BoneCP connectionPool;

    @Override
    public GOfflinePlayer getOfflinePlayerByUUID(String uuid) throws PlayerNotFoundException, StorageError {
        try (Connection connection = connectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM "+ DatabaseKeys.PLAYERS_TABLE +" WHERE ? = ?");
            preparedStatement.setString(1, DatabaseKeys.UUID.getKey());
            preparedStatement.setString(2, uuid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.getFetchSize() == 0) throw new PlayerNotFoundException("Could not find offline player!", null, null);
            resultSet.first();
            List<String> usernames = decodeUsernames(resultSet.getString(DatabaseKeys.USERNAMES.getKey()));
            int kills = resultSet.getInt(DatabaseKeys.KILLS.getKey());
            int deaths = resultSet.getInt(DatabaseKeys.DEATHS.getKey());
            int wins = resultSet.getInt(DatabaseKeys.WINS.getKey());
            int totalgames = resultSet.getInt(DatabaseKeys.TOTAL_GAMES.getKey());
            int mutation_credits = resultSet.getInt(DatabaseKeys.MUTATION_CREDITS.getKey());
            int points = resultSet.getInt(DatabaseKeys.POINTS.getKey());
            String nick = resultSet.getString(DatabaseKeys.NICK.getKey());
            return new GOfflinePlayer(uuid, usernames, kills, deaths, wins, totalgames, mutation_credits, points, nick);
        } catch (SQLException e) {
            throw new StorageError("Could not complete some part of the SQL chain while getting an offline player", e);
        }
    }

    @Override
    public GOfflinePlayer getPlayerAllowNew(Player player) throws StorageError {
        String uuid = player.getUniqueId().toString();
        List<String> usernames = Arrays.asList( player.getName());
        try {
            return getOfflinePlayerByUUID(uuid);
        } catch (PlayerNotFoundException ignored) {}//Ignored because it would have returned if successful, and failure code is to be executed below.
        try (Connection connection = connectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + DatabaseKeys.PLAYERS_TABLE + " (?, ?) VALUES (?, ?)");
            preparedStatement.setString(1, DatabaseKeys.UUID.getKey());
            preparedStatement.setString(2, DatabaseKeys.USERNAMES.getKey());
            preparedStatement.setString(3, uuid);
            preparedStatement.setString(4, encodeUsernames(usernames));
        } catch (SQLException e) {
            throw new StorageError("Could not establish connection to the SQL server!", e);
        }
        return new GOfflinePlayer(uuid, usernames, 0, 0, 0, 0, 0, 0, null);
    }

    @Override
    public void savePlayer(GPlayer player) throws StorageError, PlayerNotFoundException {
        getOfflinePlayerByUUID(player.getUuid());
        try (Connection connection = connectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + DatabaseKeys.PLAYERS_TABLE + " SET ?=?,?=?,?=?,?=?,?=?,?=?,?=? WHERE ?=?");
            preparedStatement.setString(1, DatabaseKeys.DEATHS.getKey());
            preparedStatement.setInt(2, player.getDeaths());
            preparedStatement.setString(3, DatabaseKeys.TOTAL_GAMES.getKey());
            preparedStatement.setInt(4, player.getTotalGames());
            preparedStatement.setString(5, DatabaseKeys.MUTATION_CREDITS.getKey());
            preparedStatement.setInt(6, player.getMutationCredits());
            preparedStatement.setString(7, DatabaseKeys.KILLS.getKey());
            preparedStatement.setInt(8, player.getKills());
            preparedStatement.setString(9, DatabaseKeys.WINS.getKey());
            preparedStatement.setInt(10, player.getWins());
            preparedStatement.setString(11, DatabaseKeys.POINTS.getKey());
            preparedStatement.setInt(12, player.getPoints());
            preparedStatement.setString(13, DatabaseKeys.NICK.getKey());
            preparedStatement.setString(14, player.getNick());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new StorageError("Could not communicate with the SQL Server!", e);
        }
    }

    @Override
    public void startup() throws StorageError {
        BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
        config.setPassword(this.password);
        config.setUsername(this.username);
        try {
            this.connectionPool = new BoneCP(config);
        } catch (SQLException e) {
            throw new StorageError("Could not connect to MySQL!", e);
        }

        //Setup tables
        try (Connection connection = this.connectionPool.getConnection()) {
            ResultSet tables = connection.getMetaData().getTables(null, null, DatabaseKeys.PLAYERS_TABLE.getKey(), null);
            if (!tables.next()) {
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                "CREATE TABLE " + DatabaseKeys.PLAYERS_TABLE + " (" +
                                        "? MEDIUMTEXT NOT NULL DEFAULT 'NULL'," +
                                        "? MEDIUMTEXT NOT NULL DEFAULT '[]'," +
                                        "? BIGINT NULL DEFAULT 0," +
                                        "? BIGINT NULL DEFAULT 0," +
                                        "? BIGINT NULL DEFAULT 0," +
                                        "? BIGINT NULL DEFAULT 0," +
                                        "? BIGINT NULL DEFAULT 0," +
                                        "? BIGINT NULL DEFAULT 0," +
                                        "? MEDIUMTEXT NULL DEFAULT ''," +
                                        "PRIMARY KEY (?)" +
                                        ");");
                preparedStatement.setString(1, DatabaseKeys.UUID.getKey());
                preparedStatement.setString(2, DatabaseKeys.USERNAMES.getKey());
                preparedStatement.setString(3, DatabaseKeys.KILLS.getKey());
                preparedStatement.setString(4, DatabaseKeys.DEATHS.getKey());
                preparedStatement.setString(5, DatabaseKeys.WINS.getKey());
                preparedStatement.setString(6, DatabaseKeys.TOTAL_GAMES.getKey());
                preparedStatement.setString(7, DatabaseKeys.MUTATION_CREDITS.getKey());
                preparedStatement.setString(8, DatabaseKeys.POINTS.getKey());
                preparedStatement.setString(9, DatabaseKeys.NICK.getKey());
                preparedStatement.setString(10, DatabaseKeys.UUID.getKey());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new StorageError("Could not get/use a Connection object from the connection pool to perform table checks/creation! (Or there was an error in our query, which is less likely)", e);
        }
    }

    @Override
    public void shutdown() throws StorageError {
        this.connectionPool.close();
    }

    /*
    impl
     */

    private static List<String> decodeUsernames(String s) throws StorageError {
        try {
            JSONArray usernames = (JSONArray) JSONValue.parse(s);
            ArrayList<String> strings = new ArrayList<>();
            for (Object username : usernames) {
                if (!(username instanceof String)) continue;
                strings.add((String) username);
            }
            return strings;
        } catch (ClassCastException ex) {
            throw new StorageError("Invalid usernames type!", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static String encodeUsernames(List<String> usernames) {
        JSONArray array = new JSONArray();
        for (String username : usernames) {
            array.add(username);
        }
        return array.toJSONString();
    }
}