package painting.hungershield;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import painting.hungershield.config.HungerShieldConfigManager;
import painting.hungershield.screen.HungerShieldConfigScreen;

@Mod(HungerShield.MOD_ID)
public final class HungerShield {
    public static final String MOD_ID = "hungershield";

    public HungerShield(ModContainer modContainer) {
        HungerShieldConfigManager.init();
        registerClientConfigScreen(modContainer);
    }

    private static void registerClientConfigScreen(ModContainer modContainer) {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }
        IConfigScreenFactory factory = (container, parent) -> new HungerShieldConfigScreen(parent);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, factory);
    }
}
