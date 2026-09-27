package net.gameoverse.eatduration.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.gameoverse.eatduration.EatDuration;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {
    @ModifyReturnValue(method = "getUseDuration", at = @At("RETURN"))
    private int gameoverse_eat_duration$scale(int ticks, ItemStack stack, LivingEntity entity) {
        return EatDuration.scale(stack, ticks);
    }
}
