package painting.hungershield.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import painting.hungershield.config.HungerShieldConfigManager;

public class HungerShieldConfigScreen extends Screen {
    private final Screen parent;

    public HungerShieldConfigScreen(Screen parent) {
        super(Text.translatable("config.hunger_shield.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 220;
        int buttonHeight = 20;
        int centerX = (this.width - buttonWidth) / 2;
        int centerY = this.height / 2;

        ButtonWidget toggle = ButtonWidget.builder(getToggleText(HungerShieldConfigManager.isHungerShieldEnabled()), button -> {
            boolean nextEnabled = !HungerShieldConfigManager.isHungerShieldEnabled();
            HungerShieldConfigManager.setHungerShieldEnabled(nextEnabled);
            button.setMessage(getToggleText(nextEnabled));
        }).dimensions(centerX, centerY - 10, buttonWidth, buttonHeight).build();
        this.addDrawableChild(toggle);

        ButtonWidget done = ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                .dimensions(centerX, centerY + 14, buttonWidth, buttonHeight)
                .build();
        this.addDrawableChild(done);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private static Text getToggleText(boolean enabled) {
        return Text.translatable(
                "config.hunger_shield.hunger_shield",
                enabled ? Text.translatable("options.on") : Text.translatable("options.off")
        );
    }
}
