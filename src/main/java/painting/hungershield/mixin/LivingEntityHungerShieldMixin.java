package painting.hungershield.mixin;

import painting.hungershield.config.HungerShieldConfigManager;
import painting.hungershield.service.HungerShieldService;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
        if (!HungerShieldConfigManager.isHungerShieldEnabled()) {
            return;
        }
        PlayerEntity player = serverPlayer();
        if (player == null || !HungerShieldService.canShield(player, amount)) {
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
        if (!HungerShieldConfigManager.isHungerShieldEnabled()) {
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
        HungerShieldService.apply(player, healthLost);
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

}

