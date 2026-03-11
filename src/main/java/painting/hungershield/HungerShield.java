package painting.hungershield;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import painting.hungershield.config.HungerShieldConfigManager;

@Mod(HungerShield.MOD_ID)
public final class HungerShield {
    public static final String MOD_ID = "hungershield";

    public HungerShield(IEventBus modEventBus) {
        HungerShieldConfigManager.init();
    }
}
