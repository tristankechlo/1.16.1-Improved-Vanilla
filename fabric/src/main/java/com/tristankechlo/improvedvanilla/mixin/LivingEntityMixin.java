package com.tristankechlo.improvedvanilla.mixin;

import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(at = @At(value = "TAIL"), method = "dropAllDeathLoot")
    private void dropAllDeathLoot$improvedVanilla(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
        Entity source = damageSource.getEntity();
        if (source == null) {
            return;
        }
        // drop spawn egg on entity death
        MobDropHandler.onMobDeath(source.level(), ((LivingEntity) (Object) this), damageSource);
    }

}
