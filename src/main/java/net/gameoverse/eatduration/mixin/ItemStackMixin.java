package net.gameoverse.eatduration.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla times the chewing particles and sounds from the item's own
 * consume time, so a longer bite would start chewing late and a shorter
 * one immediately. Same rule as vanilla, measured against the scaled time.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapOperation(method = "onUseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/Consumable;shouldEmitParticlesAndSounds(I)Z"))
    private boolean gameoverse_eat_duration$chewOnScaledTime(Consumable consumable, int remaining, Operation<Boolean> original,
                                                              Level level, LivingEntity entity, int remainingArg) {
        int ticks = ((ItemStack) (Object) this).getUseDuration(entity);
        if (ticks == consumable.consumeTicks()) {
            return original.call(consumable, remaining);
        }
        int elapsed = ticks - remaining;
        return elapsed > (int) (ticks * 0.21875F) && remaining % 4 == 0;
    }
}
