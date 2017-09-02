package org.inscriptio.uhc.storage;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.player.PlayerNotFoundException;
import org.inscriptio.uhc.player.UHCOfflinePlayer;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.player.StorageError;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ALL")
@GStorageKey({"storage", "sql"})
@RequiredArgsConstructor
public final class MySQLStorage implements GStorage {
    private enum DatabaseKeys {
        PLAYERS_TABLE("players"),
        UUID("id"),
        USERNAMES("usernames"),
        POINTS("points"),
        KILLS("kills"),
        DEATHS("deaths"),
        WINS("wins"),
        TOTAL_GAMES("totalgames"),
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
    public UHCOfflinePlayer getOfflinePlayerByUUID(UUID uuid) throws PlayerNotFoundException, StorageError {
        try (Connection connection = connectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM "+ DatabaseKeys.PLAYERS_TABLE +" WHERE id = ? LIMIT 1");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.first()) throw new PlayerNotFoundException("Could not find offline player!", null, null);
            List<String> usernames = decodeLists(resultSet.getString(DatabaseKeys.USERNAMES.getKey()));
            int kills = resultSet.getInt(DatabaseKeys.KILLS.getKey());
            int deaths = resultSet.getInt(DatabaseKeys.DEATHS.getKey());
            int wins = resultSet.getInt(DatabaseKeys.WINS.getKey());
            int totalgames = resultSet.getInt(DatabaseKeys.TOTAL_GAMES.getKey());
            //int mutation_credits = resultSet.getInt(DatabaseKeys.MUTATION_CREDITS.getKey());
            int points = resultSet.getInt(DatabaseKeys.POINTS.getKey());
            String nick = resultSet.getString(DatabaseKeys.NICK.getKey());
            return new UHCOfflinePlayer(uuid, usernames, kills, deaths, wins, totalgames, /*mutation_credits,*/ points, nick);
        } catch (SQLException e) {
            throw new StorageError("Could not complete some part of the SQL chain while getting an offline player", e);
        }
    }

    @Override
    public UHCOfflinePlayer getPlayerAllowNew(Player player) throws StorageError {
        UUID uuid = player.getUniqueId();
        List<String> usernames = Arrays.asList(player.getName());
        try {
            return getOfflinePlayerByUUID(uuid);
        } catch (PlayerNotFoundException ignored) {}//Ignored because it would have returned if successful, and failure code is to be executed below.
        try (Connection connection = connectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + DatabaseKeys.PLAYERS_TABLE + " (id, usernames) VALUES (?, ?)");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, encodeLists(usernames));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new StorageError("Could not establish connection to the SQL server!", e);
        }
        return new UHCOfflinePlayer(uuid, usernames, 0, 0, 0, 0, /*0,*/ 0, null);
    }

    @Override
    public void savePlayer(UHCPlayer player) throws StorageError, PlayerNotFoundException {
        getOfflinePlayerByUUID(player.getUuid());
        try (Connection connection = connectionPool.getConnection()) { //mutationpasses=?
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + DatabaseKeys.PLAYERS_TABLE + " SET deaths=?,totalgames=?,kills=?,wins=?,points=?,nick=? WHERE id=?");
            preparedStatement.setInt(1, player.getDeaths());
            preparedStatement.setInt(2, player.getTotalGames());
            //preparedStatement.setInt(3, player.getMutationCredits());
            preparedStatement.setInt(3, player.getKills());
            preparedStatement.setInt(4, player.getWins());
            preparedStatement.setInt(5, player.getPoints());
            preparedStatement.setString(6, player.getNick());
            preparedStatement.setString(7, String.valueOf(player.getUuid()));
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
            NetUHC.getInstance().logInfoInColor(ChatColor.RED + "Connected to Database: " + ChatColor.DARK_GRAY + this.database);
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
                                        "id VARCHAR(36) NOT NULL," +
                                        "usernames MEDIUMTEXT NOT NULL," +
                                        "points BIGINT NULL DEFAULT 0," +
                                        "kills BIGINT NULL DEFAULT 0," +
                                        "deaths BIGINT NULL DEFAULT 0," +
                                        "wins BIGINT NULL DEFAULT 0," +
                                        "totalgames BIGINT NULL DEFAULT 0," +
                                        "nick MEDIUMTEXT NULL," +
                                        "PRIMARY KEY (id)" +
                                        ");");
                preparedStatement.executeUpdate();
                //"mutationpasses BIGINT NULL DEFAULT 0," +
            }
        } catch (SQLException e) {
            throw new StorageError("Could not get/use a Connection object from the connection pool to perform table checks/creation! (Or there was an error in our query, which is less likely)", e);
        }
    }

    @Override
    public void shutdown() throws StorageError {
        this.connectionPool.close();
    }

//    @Override
//    public boolean isInDatabase(UHCPlayer player) throws SQLException {
//        Connection connection = this.connectionPool.getConnection();
//        String query = "SELECT * FROM `players` WHERE id = " + "'" + player.getUuid() + "'"; // gets uuid
//        PreparedStatement st = connection.prepareStatement(query); // prepares for the running of the query
//        ResultSet rs = st.executeQuery(); // runs the query
//        if (!rs.first()) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    /*
    impl
     */

    private static List<String> decodeLists(String s) throws StorageError {
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
    private static String encodeLists(List<String> usernames) {
        JSONArray array = new JSONArray();
        array.addAll(usernames);
        return array.toJSONString();
    }
}