package painting.hungershield;

import net.fabricmc.api.ModInitializer;
import painting.hungershield.config.HungerShieldConfigManager;

public class HungerShield implements ModInitializer {
    @Override
    public void onInitialize() {
        HungerShieldConfigManager.init();
    }
}
