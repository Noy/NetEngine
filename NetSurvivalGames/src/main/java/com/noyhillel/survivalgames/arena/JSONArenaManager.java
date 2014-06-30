package com.noyhillel.survivalgames.arena;

import com.noyhillel.survivalgames.game.lobby.GameLobby;
import lombok.Getter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public final class JSONArenaManager implements ArenaManager {

    /* Constant Key Enum */

    private static enum ArenaKeys {
        X("x"),
        Y("y"),
        Z("z"),
        PITCH("pitch"),
        YAW("yaw"),
        META("meta"),
        META_NAME("name"),
        META_AUTHORS("authors"),
        META_SOCIAL_LINK("link"),
        POINTS_CORNICOPIA_SPAWN("cornicopia_spawn"),
        POINTS_TIER1_CHEST("tier1_chest"),
        POINTS_TIER2_CHEST("tier2_chest"),
        ARENA_FILE_NAME("arena.json"),
        WORLD_ZIP_NAME("world.zip"),
        GAME_LOBBY_NAME("lobby"),
        LOBBY_SPAWN_POINTS("lobby_spawn");

        private final String key;

        @Override
        public String toString() {
            return key;
        }
        ArenaKeys(String key) {
            this.key = key;
        }
    }

    /* Variables */

    private final File arenaDirectory;
    @Getter private List<Arena> arenas;
    @Getter private GameLobby gameLobby;
    private final Logger logger;

    public JSONArenaManager(File arenaDirectory, Logger logger) throws ArenaException {
        if ((!arenaDirectory.exists() && !arenaDirectory.mkdir()) || !arenaDirectory.isDirectory()) throw new ArenaException(null, null, "Could not create the arenas directory!");
        this.arenaDirectory = arenaDirectory;
        this.logger = logger;
    }

    /*
     - Arenas are in a "Arena folder" passed to the constructor of the manager
     - Arenas each have their own folder, with an arena.json file, and a zip for the world
     - These JSON files can be read as arenas, and the world's temporary locations are stored in the Arena object
     - The ArenaMeta object will contain author, and other details about the map. This is loaded from arena.json
     */

    @Override
    public void saveArena(Arena arena) throws ArenaException {
        //Save the arena meta file
        ArenaMeta meta = arena.getMeta();
        String name = meta.getName();
        if (name.equals(ArenaKeys.GAME_LOBBY_NAME.key)) throw new ArenaException(arena, null, "Invalid name for arena! Overwrites Lobby name!");
        File directoryPath = new File(arenaDirectory.getPath(), name);
        JSONObject jsonObject;
        try {
            jsonObject = encodeArena(arena);
        } catch (Exception e) {
            throw new ArenaException(arena, e, "Could not create an arena JSONObject");
        }
        save(directoryPath, jsonObject, arena);
        logger.info("Saved arena " + meta.getName() + " by " + meta.getAuthors());
    }

    private void save(File directoryPath, JSONObject jsonObject, WorldStrapped worldStrapped) throws ArenaException {
        if ((directoryPath.exists() && !directoryPath.isDirectory()) || !directoryPath.mkdir()) throw new ArenaException(worldStrapped, null, "Could not create the arena directory!");
        String jsonString = jsonObject.toJSONString();
        File jsonFileLocation = new File(directoryPath, String.valueOf(ArenaKeys.ARENA_FILE_NAME));
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(jsonFileLocation));
            writer.write(jsonString);
            writer.flush();
        } catch (IOException e) {
            throw new ArenaException(worldStrapped, e, "Could not save the arena file!");
        } finally { if (writer != null) try {writer.close();} catch (IOException ignored) {} }
        //Now save the world
        ZipFile zipFileHandle;
        try {
            File zipFileDest = new File(directoryPath, String.valueOf(ArenaKeys.WORLD_ZIP_NAME));
            zipFileHandle = new ZipFile(zipFileDest);
        } catch (ZipException e) {
            throw new ArenaException(worldStrapped, e, "Could not create a zipfile!");
        }
        File worldFolder = worldStrapped.getLoadedWorld().getWorldFolder();
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        File[] files = worldFolder.listFiles();
        if (files == null) throw new ArenaException(worldStrapped, null, "World folder does not contain any files!?");
        for (File file : files) {
            try {
                if (file.isDirectory()) zipFileHandle.addFolder(file, zipParameters);
                else zipFileHandle.addFile(file, zipParameters);
            } catch (ZipException e) {
                throw new ArenaException(worldStrapped, e, "Could not add files to a zip file!");
            }
        }
    }

    @Override
    public void reloadArenas() throws ArenaException {
        loadArenasFromFile();
        loadGameLobbyFromFile();
    }

    private void loadArenasFromFile() throws ArenaException {
        this.arenas = new ArrayList<>();
        File[] files = this.arenaDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && new File(pathname, String.valueOf(ArenaKeys.ARENA_FILE_NAME)).exists() && !pathname.getName().equals(ArenaKeys.GAME_LOBBY_NAME.key) && new File(pathname, String.valueOf(ArenaKeys.WORLD_ZIP_NAME)).exists();
            }
        });
        if (files == null) return;
        for (File file : files) {
            Arena arenaFromDirectory = getArenaFromDirectory(file);
            this.arenas.add(arenaFromDirectory);
            ArenaMeta meta = arenaFromDirectory.getMeta();
            logger.info("Loaded arena - " + meta.getName() + " by " + meta.getAuthors() + "!");
        }
        logger.info("Loaded all arenas!");
    }

    private void loadGameLobbyFromFile() throws ArenaException {
        File directoryPath = new File(arenaDirectory.getPath(), String.valueOf(ArenaKeys.GAME_LOBBY_NAME));
        if (!directoryPath.exists() || !directoryPath.isDirectory()) throw new ArenaException(null, null, "Folder for lobby does not exist!");
        JSONObject arenaFileFrom = getArenaFileFrom(directoryPath);
        List<Point> spawnPoints = getPointsFromList((JSONArray) arenaFileFrom.get(ArenaKeys.LOBBY_SPAWN_POINTS.toString()));
        File file = new File(directoryPath, ArenaKeys.WORLD_ZIP_NAME.toString());
        if (!file.exists() || !file.isFile()) throw new ArenaException(null, null, "ZipFile for world does not exist!");
        logger.info("Loaded game lobby!");
        this.gameLobby = new GameLobby(spawnPoints,  file);
    }

    @Override
    public void saveGameLobby(GameLobby gameLobby) throws ArenaException {
        List<Point> spawnPoints = gameLobby.getSpawnPoints().getPoints();
        JSONObject object = new JSONObject();
        object.put(ArenaKeys.LOBBY_SPAWN_POINTS.toString(), encodePointList(spawnPoints));
        File directoryPath = new File(arenaDirectory.getPath(), String.valueOf(ArenaKeys.GAME_LOBBY_NAME));
        save(directoryPath, object, gameLobby);
        logger.info("Saved game lobby!");
    }

    /*
    IMPL - C/U
     */

    private static JSONObject encodeArena(Arena arena) throws Exception /*Forces the calling method to catch any null pointers/indexoutofbounds/etc (although wouldn't expect the second)*/ {
        JSONObject object = new JSONObject();
        object.put(ArenaKeys.POINTS_CORNICOPIA_SPAWN, encodePointList(arena.getCornicopiaSpawns().getPoints()));
        object.put(ArenaKeys.POINTS_TIER1_CHEST, encodePointList(arena.getTier1().getPoints()));
        object.put(ArenaKeys.POINTS_TIER2_CHEST, encodePointList(arena.getTier2().getPoints()));
        object.put(ArenaKeys.META, encodeMeta(arena.getMeta()));
        return object;
    }

    private static JSONArray encodePointList(List<Point> points) {
        JSONArray array = new JSONArray();
        for (Point point : points) {
            JSONObject jsonPoint = new JSONObject();
            jsonPoint.put(ArenaKeys.X, point.getX());
            jsonPoint.put(ArenaKeys.Y, point.getY());
            jsonPoint.put(ArenaKeys.Z, point.getZ());
            jsonPoint.put(ArenaKeys.PITCH, point.getPitch());
            jsonPoint.put(ArenaKeys.YAW, point.getYaw());
            array.add(jsonPoint);
        }
        return array;
    }

    private static JSONObject encodeMeta(ArenaMeta meta) {
        JSONObject jsonMeta = new JSONObject();
        jsonMeta.put(ArenaKeys.META_NAME, meta.getName());
        jsonMeta.put(ArenaKeys.META_AUTHORS, meta.getAuthors());
        jsonMeta.put(ArenaKeys.META_SOCIAL_LINK, meta.getSocialLink());
        return jsonMeta;
    }

    /*
    IMPL - R
     */

    private static Arena getArenaFromDirectory(File file) throws ArenaException {
        JSONObject object = getArenaFileFrom(file);
        try {
            List<Point> cornicopiaSpawn = getPointsFromList((JSONArray) object.get(ArenaKeys.POINTS_CORNICOPIA_SPAWN.toString()));
            List<Point> tier1 = getPointsFromList((JSONArray) object.get(ArenaKeys.POINTS_TIER1_CHEST.toString()));
            List<Point> tier2 = getPointsFromList((JSONArray) object.get(ArenaKeys.POINTS_TIER2_CHEST.toString()));
            ArenaMeta arenaMeta = parseMeta((JSONObject) object.get(ArenaKeys.META.toString()));
            return new Arena(cornicopiaSpawn, tier1, tier2, arenaMeta, new File(file, String.valueOf(ArenaKeys.WORLD_ZIP_NAME.toString())));
        }
        catch (ClassCastException ex) {
            throw new ArenaException(null, ex, "Could not cast a class in the reading of an arena!");
        }
    }

    private static JSONObject getArenaFileFrom(File file) throws ArenaException {
        File arenaFile = new File(file, String.valueOf(ArenaKeys.ARENA_FILE_NAME));
        JSONObject object;
        try {
            BufferedReader bufferedInputStream = new BufferedReader(new FileReader(arenaFile));
            String line;
            StringBuilder builder = new StringBuilder();
            String ls = System.getProperty("line.separator");
            while ((line = bufferedInputStream.readLine()) != null) {
                builder.append(line).append(ls);
            }
            String s = builder.toString();
            object = (JSONObject) JSONValue.parse(s);
        } catch (IOException e) {
            throw new ArenaException(null, e, "Could not read the file containing the arena data");
        } catch (ClassCastException ex) {
            throw new ArenaException(null, ex, "Could not cast JSON object to JSONObject");
        }
        return object;
    }

    private static List<Point> getPointsFromList(JSONArray array) {
        ArrayList<Point> points = new ArrayList<>();
        for (Object o : array) {
            if (!(o instanceof JSONObject)) continue;
            JSONObject jsonPoint = (JSONObject)o;
            try {
                Double x = (Double)jsonPoint.get(ArenaKeys.X.toString());
                Double y = (Double)jsonPoint.get(ArenaKeys.Y.toString());
                Double z = (Double)jsonPoint.get(ArenaKeys.Z.toString());
                Double pitch = (Double)jsonPoint.get(ArenaKeys.PITCH.toString());
                Double yaw = (Double)jsonPoint.get(ArenaKeys.YAW.toString());
                points.add(Point.of(x, y, z, pitch.floatValue(), yaw.floatValue()));
            }
            catch (ClassCastException ignored) {}
        }
        return points;
    }

    private static ArenaMeta parseMeta(JSONObject jsonMeta) {
        try {
            String name = (String) jsonMeta.get(ArenaKeys.META_NAME.toString());
            JSONArray authorsArray = (JSONArray) jsonMeta.get(ArenaKeys.META_AUTHORS.toString());
            List<String> authorsList = new ArrayList<>();
            for (Object o : authorsArray) {
                if (!(o instanceof String)) continue;
                String partOf = (String)o;
                authorsList.add(partOf);
            }
            String socialLink = (String) jsonMeta.get(ArenaKeys.META_SOCIAL_LINK.toString());
            return new ArenaMeta(name, authorsList, socialLink);
        }
        catch (ClassCastException ignored) {
            return null;
        }
    }
}
