package painting.hungershield.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import painting.hungershield.access.HungerShieldCarryAccess;

@Mixin(Player.class)
public class PlayerEntityHungerShieldMixin implements HungerShieldCarryAccess {
    @Unique
    private float hunger_shield$hungerShieldCarry;

    @Override
    public float hunger_shield$getHungerShieldCarry() {
        return this.hunger_shield$hungerShieldCarry;
    }

    @Override
    public void hunger_shield$setHungerShieldCarry(float carry) {
        this.hunger_shield$hungerShieldCarry = carry;
    }
}
