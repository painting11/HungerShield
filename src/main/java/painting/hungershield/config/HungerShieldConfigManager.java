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
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static boolean loadOrCreate() {
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            save(DEFAULT_ENABLED);
            return DEFAULT_ENABLED;
        }

        try {
            String json = Files.readString(path);
            HungerShieldConfig parsed = GSON.fromJson(json, HungerShieldConfig.class);
            if (parsed == null) {
                LOGGER.warn(Text.translatable("log.hunger_shield.config.read_failed", path.toString()).getString());
                return DEFAULT_ENABLED;
            }
            return parsed.hunger_shield;
        } catch (IOException | JsonParseException e) {
            LOGGER.warn(Text.translatable("log.hunger_shield.config.read_failed", path.toString()).getString(), e);
            return DEFAULT_ENABLED;
        }
    }

    private static void save(boolean enabled) {
        Path path = getConfigPath();
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
            LOGGER.warn(Text.translatable("log.hunger_shield.config.write_failed", path.toString()).getString(), e);
        }
    }
}
