package com.tristankechlo.improvedvanilla.mixin;

import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(at = @At(value = "TAIL"), method = "dropAllDeathLoot")
    private void dropAllDeathLoot$improvedVanilla(DamageSource damageSource, CallbackInfo info) {
        Entity source = damageSource.getEntity();
        if (source == null) {
            return;
        }
        int lootingLevel = 0;
        if (source instanceof Player) {
            lootingLevel = EnchantmentHelper.getMobLooting((LivingEntity) source);
        }
        // drop spawn egg on entity death
        MobDropHandler.onMobDeath(source.getLevel(), ((LivingEntity) (Object) this), damageSource, lootingLevel);
    }

}
