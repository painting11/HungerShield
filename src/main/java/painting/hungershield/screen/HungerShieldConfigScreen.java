package painting.hungershield.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import painting.hungershield.config.HungerShieldConfigManager;

public final class HungerShieldConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int TOGGLE_OFFSET_Y = -10;
    private static final int DONE_OFFSET_Y = 14;
    private final Screen parent;

    public HungerShieldConfigScreen(Screen parent) {
        super(Component.translatable("config.hunger_shield.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = (this.width - BUTTON_WIDTH) / 2;
        int centerY = this.height / 2;
        this.addRenderableWidget(createToggleButton(centerX, centerY + TOGGLE_OFFSET_Y));
        this.addRenderableWidget(createDoneButton(centerX, centerY + DONE_OFFSET_Y));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static Component getToggleText(boolean enabled) {
        return Component.translatable(
                "config.hunger_shield.hunger_shield",
                enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF
        );
    }

    private Button createToggleButton(int x, int y) {
        return Button.builder(getToggleText(HungerShieldConfigManager.isHungerShieldEnabled()), button -> {
            boolean nextEnabled = !HungerShieldConfigManager.isHungerShieldEnabled();
            HungerShieldConfigManager.setHungerShieldEnabled(nextEnabled);
            button.setMessage(getToggleText(nextEnabled));
        }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
    }

    private Button createDoneButton(int x, int y) {
        return Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
    }
}
