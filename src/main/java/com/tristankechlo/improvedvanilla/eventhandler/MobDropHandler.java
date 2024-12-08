package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MobDropHandler {

    @SubscribeEvent
    public void onMobDeath(LivingDropsEvent event) {
        final EntityLivingBase entity = event.getEntityLiving();
        final World world = entity.world;
        if (world.isRemote) {
            return;
        }

        final boolean onlyWhenKilledByPlayer = ImprovedVanillaConfig.MOB_DROP.dropOnlyWhenKilledByPlayer.get();
        final int dropChance = ImprovedVanillaConfig.MOB_DROP.mobSpawnEggDropChance.get();
        final Entity source = event.getSource().getTrueSource();
        final BlockPos pos = entity.getPosition();

        // killed by player and onlyWhenKilledByPlayer is true
        if ((source instanceof EntityPlayerMP) && onlyWhenKilledByPlayer) {
            // drop only when entity was killed by a player
            EntityPlayerMP player = (EntityPlayerMP) source;
            if (player.isSpectator()) {
                return;
            }
            handleKilledByPlayer(world, event, entity, dropChance);
        } else if (!onlyWhenKilledByPlayer) {

            // if the mob was killed by player anyway
            if (source instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) source;
                if (player.isSpectator()) {
                    return;
                }
                handleKilledByPlayer(world, event, entity, dropChance);
            } else {
                // not killed by player
                if (dropChance <= 0 || dropChance > 100) {
                    return;
                }
                if (Math.random() < ((double) dropChance / 100)) {
                    NBTTagCompound compound = entity.serializeNBT();
                    ItemStack stack = ImprovedVanilla.getMonsterEgg(compound.getString("id"));
                    stack.setCount(1);
                    EntityItem itemEntity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    event.getDrops().add(itemEntity);
                }
            }
        }
    }

    private static void handleKilledByPlayer(World world, LivingDropsEvent event, EntityLivingBase entity, int dropChance) {
        int lootingLevel = event.getLootingLevel();
        final boolean lootingAffective = ImprovedVanillaConfig.MOB_DROP.lootingAffective.get();
        BlockPos pos = entity.getPosition();

        int count = 0;
        if (lootingAffective && lootingLevel >= 1) {
            // foreach lootingLevel there's an additional chance to drop the egg
            for (int i = 0; i < (1 + lootingLevel); i++) {
                if (Math.random() < ((double) dropChance / 100)) {
                    count++;
                }
            }
        } else {
            if (Math.random() < ((double) dropChance / 100)) {
                count++;
            }
        }
        if (count > 0) {
            NBTTagCompound compound = entity.serializeNBT();
            ItemStack stack = ImprovedVanilla.getMonsterEgg(compound.getString("id"));
            stack.setCount(count);
            EntityItem itemEntity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            itemEntity.setDefaultPickupDelay();
            event.getDrops().add(itemEntity);
        }
    }

}
