package painting.hungershield.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import painting.hungershield.access.HungerShieldCarryAccess;
import painting.hungershield.config.HungerShieldConfigManager;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHungerShieldMixin {
    @Unique
    private boolean tracking;
    @Unique
    private float healthBefore;

    @Inject(method = "hurt", at = @At("HEAD"))
    @Unique
    private void onDamageStart(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.tracking = false;
        Player player = serverPlayer();
        if (player == null || !Float.isFinite(amount) || amount <= 0.0f || !HungerShieldConfigManager.isHungerShieldEnabled()) {
            return;
        }
        if (player.getFoodData().getFoodLevel() <= 0) {
            return;
        }
        this.healthBefore = player.getHealth();
        this.tracking = true;
    }

    @Inject(method = "hurt", at = @At("TAIL"))
    @Unique
    private void onDamageEnd(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.tracking) {
            return;
        }
        this.tracking = false;
        Player player = serverPlayer();
        if (player == null || !HungerShieldConfigManager.isHungerShieldEnabled()) {
            return;
        }
        float healthLost = this.healthBefore - player.getHealth();
        if (!Float.isFinite(healthLost) || healthLost <= 0.0f) {
            return;
        }
        applyHungerShield(player, healthLost);
    }

    @Unique
    private Player serverPlayer() {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return null;
        }
        if (player.level().isClientSide) {
            return null;
        }
        return player;
    }

    @Unique
    private static void applyHungerShield(Player player, float healthLost) {
        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        HungerShieldCarryAccess access = (HungerShieldCarryAccess) player;
        if (foodLevel <= 0) {
            access.hunger_shield$setHungerShieldCarry(0.0f);
            return;
        }
        if (!Float.isFinite(healthLost) || healthLost <= 0.0f) {
            return;
        }
        float absorb = Math.min(healthLost, (float) foodLevel);
        if (!Float.isFinite(absorb) || absorb <= 0.0f) {
            return;
        }

        float consume = absorb + access.hunger_shield$getHungerShieldCarry();
        if (!Float.isFinite(consume) || consume < 0.0f) {
            access.hunger_shield$setHungerShieldCarry(0.0f);
            return;
        }
        int consumeWhole = (int) consume;
        float carry = consume - consumeWhole;

        if (consumeWhole > 0) {
            int newFoodLevel = Math.max(0, foodLevel - consumeWhole);
            foodData.setFoodLevel(newFoodLevel);
            foodData.setSaturation(Math.min(foodData.getSaturationLevel(), (float) newFoodLevel));
            if (newFoodLevel == 0) {
                carry = 0.0f;
            }
        }

        access.hunger_shield$setHungerShieldCarry(carry);
        player.heal(absorb);
    }
}
