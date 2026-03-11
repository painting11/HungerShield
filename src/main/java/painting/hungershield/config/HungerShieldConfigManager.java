package painting.hungershield.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class HungerShieldConfigManager {
    private static final String FILE_NAME = "hunger_shield.json";
    private static final boolean DEFAULT_ENABLED = true;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("hunger_shield");
    private static final Object LOCK = new Object();
    private static volatile boolean hungerShieldEnabled = DEFAULT_ENABLED;

    private HungerShieldConfigManager() {
    }

    public static boolean isHungerShieldEnabled() {
        return hungerShieldEnabled;
    }

    public static void setHungerShieldEnabled(boolean enabled) {
        synchronized (LOCK) {
            if (hungerShieldEnabled == enabled) {
                return;
            }
            hungerShieldEnabled = enabled;
            save(enabled);
        }
    }

    public static void init() {
        synchronized (LOCK) {
            hungerShieldEnabled = loadOrCreate();
        }
    }

    private static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    private static boolean loadOrCreate() {
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            save(path, DEFAULT_ENABLED);
            return DEFAULT_ENABLED;
        }
        try {
            HungerShieldConfig parsed = GSON.fromJson(Files.readString(path), HungerShieldConfig.class);
            if (parsed != null) {
                return parsed.hunger_shield;
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.warn("Failed to read config file: {}", path, e);
        }
        return DEFAULT_ENABLED;
    }

    private static void save(boolean enabled) {
        save(getConfigPath(), enabled);
    }

    private static void save(Path path, boolean enabled) {
        try {
            Files.createDirectories(path.getParent());
            HungerShieldConfig config = new HungerShieldConfig();
            config.hunger_shield = enabled;
            String json = GSON.toJson(config);
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(tmp, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to write config file: {}", path, e);
        }
    }
}
