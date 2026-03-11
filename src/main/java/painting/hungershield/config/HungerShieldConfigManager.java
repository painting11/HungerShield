package painting.hungershield.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class HungerShieldConfigManager {
    private static final String FILE_NAME = "hunger_shield.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("hunger_shield");
    private static final Object LOCK = new Object();
    private static volatile HungerShieldConfig CONFIG = new HungerShieldConfig();

    private HungerShieldConfigManager() {
    }

    public static HungerShieldConfig get() {
        synchronized (LOCK) {
            return copy(CONFIG);
        }
    }

    public static boolean isHungerShieldEnabled() {
        synchronized (LOCK) {
            return CONFIG.isHungerShieldEnabled();
        }
    }

    public static void setHungerShieldEnabled(boolean enabled) {
        synchronized (LOCK) {
            CONFIG.setHungerShieldEnabled(enabled);
            save(CONFIG);
        }
    }

    public static void init() {
        synchronized (LOCK) {
            CONFIG = loadOrCreate();
        }
    }

    public static void save() {
        synchronized (LOCK) {
            save(CONFIG);
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static HungerShieldConfig loadOrCreate() {
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            HungerShieldConfig created = new HungerShieldConfig();
            save(created);
            return created;
        }

        try {
            String json = Files.readString(path);
            HungerShieldConfig parsed = GSON.fromJson(json, HungerShieldConfig.class);
            if (parsed == null) {
                HungerShieldConfig fallback = new HungerShieldConfig();
                save(fallback);
                return fallback;
            }
            return parsed;
        } catch (IOException | JsonParseException e) {
            HungerShieldConfig fallback = new HungerShieldConfig();
            save(fallback);
            LOGGER.warn(Text.translatable("log.hunger_shield.config.read_failed", path.toString()).getString(), e);
            return fallback;
        }
    }

    private static void save(HungerShieldConfig config) {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            String json = GSON.toJson(config);
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(tmp, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.warn(Text.translatable("log.hunger_shield.config.write_failed", path.toString()).getString(), e);
        }
    }

    private static HungerShieldConfig copy(HungerShieldConfig source) {
        HungerShieldConfig copied = new HungerShieldConfig();
        copied.setHungerShieldEnabled(source.isHungerShieldEnabled());
        return copied;
    }
}
