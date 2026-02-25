package painting.hungershield.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import painting.hungershield.screen.HungerShieldConfigScreen;

public class HungerShieldModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return HungerShieldConfigScreen::new;
    }
}