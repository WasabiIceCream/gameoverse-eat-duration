package net.gameoverse.eatduration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * config/gameoverse_eat_duration.json, written with defaults on first boot.
 * Restart to apply edits. The server sends its values to each client on join
 * (see {@link SyncPayload}), so players always eat on the server's timing even
 * though AutoModpack leaves their own copy of config/ alone after the first
 * download.
 * <p>
 * An item's eating time becomes its own time x (offset + slope x nutrition),
 * clamped to [minSeconds, maxSeconds]. The defaults leave nutrition 4 (an
 * apple) at vanilla speed.
 */
public final class EatDurationConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
        .resolve("gameoverse_eat_duration.json");

    private static EatDurationConfig instance;
    private static EatDurationConfig fromServer;

    public double offset = 0.5;
    public double slope = 0.125;
    public double minSeconds = 0.6;
    public double maxSeconds = 4.0;

    public static synchronized EatDurationConfig get() {
        if (fromServer != null) {
            return fromServer;
        }
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    /** The server's values while connected to one, null to go back to the local file. */
    public static synchronized void setFromServer(EatDurationConfig config) {
        fromServer = config;
    }

    private static EatDurationConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                EatDurationConfig loaded = GSON.fromJson(reader, EatDurationConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (Exception e) {
                EatDuration.LOGGER.warn("Couldn't read {}, using defaults", PATH, e);
            }
        }

        EatDurationConfig defaults = new EatDurationConfig();
        defaults.save();
        return defaults;
    }

    private void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            EatDuration.LOGGER.warn("Couldn't write {}", PATH, e);
        }
    }
}
