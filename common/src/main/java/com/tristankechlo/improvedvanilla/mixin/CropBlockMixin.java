package com.tristankechlo.improvedvanilla.mixin;

import com.tristankechlo.improvedvanilla.platform.CropBlockHelper;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin implements CropBlockHelper {

    @Override
    public IntegerProperty getAgeProp() {
        return this.getAgeProperty();
    }

    @Shadow
    protected abstract IntegerProperty getAgeProperty();

}
