package com.noyhillel.survivalgames.arena;

import com.noyhillel.networkengine.util.utils.RandomUtils;
import com.noyhillel.survivalgames.SurvivalGames;
import lombok.Data;
import lombok.NonNull;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Item;

import java.io.File;

@Data
public abstract class WorldStrapped {
    @NonNull private File zippedWorldFile;
    private World loadedWorld = null;

    protected WorldStrapped(File file) {
        this.zippedWorldFile = file;
    }

    protected  WorldStrapped(World world) {
        this.loadedWorld = world;
        this.zippedWorldFile = null;
    }

    public void loadWorld() throws ArenaException {
        if (loadedWorld != null) throw new IllegalStateException("Cannot load world that is already loaded!");
        String worldName = RandomUtils.getRandomString(16);
        File destinationWorld;
        do {
            destinationWorld = new File(Bukkit.getWorldContainer(), worldName);
        } while (destinationWorld.exists() && !destinationWorld.isDirectory());
        if (!destinationWorld.mkdir()) throw new ArenaException(this, null, "Could not create world directory for loading!");
        ZipFile zippedWorld;
        try {
            zippedWorld = new ZipFile(zippedWorldFile);
            zippedWorld.extractAll(destinationWorld.getPath());
        } catch (ZipException e) {
            throw new ArenaException(this, e, "Could not extract zip file to the world file!");
        }
        this.loadedWorld = Bukkit.createWorld(WorldCreator.name(worldName));
        this.loadedWorld.setDifficulty(Difficulty.NORMAL);
        this.loadedWorld.setTime(0);
        this.loadedWorld.setAutoSave(false);
        this.loadedWorld.setStorm(false);
        this.loadedWorld.setGameRuleValue("doMobLoot", "false");
        this.loadedWorld.setGameRuleValue("doMobSpawning", "false");
        this.loadedWorld.setGameRuleValue("keepInventory", "false");
        SurvivalGames.getInstance().getLogger().info("Loaded world " + loadedWorld.getName());
    }

    public void unloadWorld() throws ArenaException {
        Bukkit.unloadWorld(loadedWorld, false);
        try {
            deleteDirectory(loadedWorld.getWorldFolder());
        } catch (Exception e) {
            throw new ArenaException(this, e, "Could not delete world folder on unload!");
        }
        loadedWorld = null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                deleteDirectory(new File(dir, aChildren));
            }
        }
        dir.delete();
    }

    public void cleanupDrops() {
        for (Item item : loadedWorld.getEntitiesByClass(Item.class)) {
            item.remove();
        }
    }

    public boolean isLoaded() {
        return this.loadedWorld != null;
    }
}
