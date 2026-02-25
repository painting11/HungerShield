package painting.hungershield.mixin;

import painting.hungershield.config.HungerShieldConfigManager;
import painting.hungershield.access.HungerShieldCarryAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHungerShieldMixin {
    @Unique
    private boolean tracking;
    @Unique
    private float healthBefore;

    @Inject(method = "damage", at = @At("HEAD"))
    @Unique
    private void onDamageStart(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.tracking = false;
        if (!HungerShieldConfigManager.get().hunger_shield) {
            return;
        }
        PlayerEntity player = serverPlayer();
        if (player == null || amount <= 0.0f || player.getHungerManager().getFoodLevel() <= 0) {
            return;
        }
        this.healthBefore = player.getHealth();
        this.tracking = true;
    }

    @Inject(method = "damage", at = @At("TAIL"))
    @Unique
    private void onDamageEnd(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.tracking) {
            return;
        }
        this.tracking = false;
        if (!HungerShieldConfigManager.get().hunger_shield) {
            return;
        }
        PlayerEntity player = serverPlayer();
        if (player == null) {
            return;
        }

        float healthLost = this.healthBefore - player.getHealth();
        if (healthLost <= 0.0f) {
            return;
        }
        applyHungerShield(player, healthLost);
    }

    @Unique
    private PlayerEntity serverPlayer() {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof PlayerEntity player)) {
            return null;
        }
        if (player.getWorld().isClient) {
            return null;
        }
        return player;
    }

    @Unique
    private static void applyHungerShield(PlayerEntity player, float healthLost) {
        HungerManager hungerManager = player.getHungerManager();
        int foodLevel = hungerManager.getFoodLevel();
        if (foodLevel <= 0) {
            ((HungerShieldCarryAccess) player).hunger_shield$setHungerShieldCarry(0.0f);
            return;
        }

        float absorb = Math.min(healthLost, (float) foodLevel);
        if (absorb <= 0.0f) {
            return;
        }

        HungerShieldCarryAccess access = (HungerShieldCarryAccess) player;
        float consume = absorb + access.hunger_shield$getHungerShieldCarry();
        int consumeWhole = (int) consume;
        float consumeCarry = consume - (float) consumeWhole;

        if (consumeWhole > 0) {
            int newFoodLevel = foodLevel - consumeWhole;
            if (newFoodLevel < 0) {
                newFoodLevel = 0;
            }
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

