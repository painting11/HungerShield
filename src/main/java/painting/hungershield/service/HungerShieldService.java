package painting.hungershield.service;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import painting.hungershield.access.HungerShieldCarryAccess;

public final class HungerShieldService {
    private HungerShieldService() {
    }

    public static boolean canShield(PlayerEntity player, float incomingDamage) {
        return incomingDamage > 0.0f && player.getHungerManager().getFoodLevel() > 0;
    }

    public static void apply(PlayerEntity player, float healthLost) {
        HungerManager hungerManager = player.getHungerManager();
        int foodLevel = hungerManager.getFoodLevel();
        HungerShieldCarryAccess access = (HungerShieldCarryAccess) player;
        if (foodLevel <= 0) {
            access.hunger_shield$setHungerShieldCarry(0.0f);
            return;
        }

        float absorb = Math.min(healthLost, (float) foodLevel);
        if (absorb <= 0.0f) {
            return;
        }

        float consume = absorb + access.hunger_shield$getHungerShieldCarry();
        int consumeWhole = (int) consume;
        float consumeCarry = consume - (float) consumeWhole;
        if (consumeWhole > 0) {
            int newFoodLevel = Math.max(0, foodLevel - consumeWhole);
            hungerManager.setFoodLevel(newFoodLevel);
            hungerManager.setSaturationLevel(Math.max(0.0f, Math.min(hungerManager.getSaturationLevel(), (float) newFoodLevel)));
            if (newFoodLevel == 0) {
                consumeCarry = 0.0f;
            }
        }

        access.hunger_shield$setHungerShieldCarry(consumeCarry);
        player.heal(absorb);
    }
}
