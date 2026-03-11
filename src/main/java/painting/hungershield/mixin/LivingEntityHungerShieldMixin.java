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
    private boolean tracking = false;
    @Unique
    private float healthBefore;

    @Inject(method = "damage", at = @At("HEAD"))
    @Unique
    private void onDamageStart(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.tracking = false;
        PlayerEntity player = serverPlayer();
        if (player == null || !Float.isFinite(amount) || amount <= 0.0f || !HungerShieldConfigManager.isHungerShieldEnabled()) {
            return;
        }
        if (player.getHungerManager().getFoodLevel() <= 0) {
            return;
        }
        this.healthBefore = player.getHealth();
        this.tracking = true;
    }

    @Inject(method = "damage", at = @At("TAIL"))
    @Unique
    private void onDamageEnd(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.tracking) return;
        this.tracking = false;
        PlayerEntity player = serverPlayer();
        if (player == null || !HungerShieldConfigManager.isHungerShieldEnabled()) return;

        float healthLost = this.healthBefore - player.getHealth();
        if (!Float.isFinite(healthLost) || healthLost <= 0.0f) return;
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
        if (!Float.isFinite(healthLost) || healthLost <= 0.0f) {
            return;
        }
        float absorb = Math.min(healthLost, (float) foodLevel);
        if (!Float.isFinite(absorb) || absorb <= 0.0f) return;

        HungerShieldCarryAccess access = (HungerShieldCarryAccess) player;
        float consume = absorb + access.hunger_shield$getHungerShieldCarry();
        if (!Float.isFinite(consume) || consume < 0.0f) {
            access.hunger_shield$setHungerShieldCarry(0.0f);
            return;
        }
        int consumeWhole = (int) consume;
        float carry = consume - consumeWhole;

        if (consumeWhole > 0) {
            int newFoodLevel = Math.max(0, foodLevel - consumeWhole);
            hungerManager.setFoodLevel(newFoodLevel);
            hungerManager.setSaturationLevel(Math.min(hungerManager.getSaturationLevel(), (float) newFoodLevel));
            if (newFoodLevel == 0) carry = 0.0f;
        }

        access.hunger_shield$setHungerShieldCarry(carry);
        player.heal(absorb);
    }
}

