package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MobDropHandler {

    @SubscribeEvent
    public void onMobDeath(final LivingDropsEvent event) {
        final EntityLivingBase entity = event.getEntityLiving();
        final World world = entity.world;
        if (world.isRemote) {
            return;
        }

        final boolean onlyWhenKilledByPlayer = ImprovedVanillaConfig.MOB_DROP.dropOnlyWhenKilledByPlayer.get();
        final int dropchance = ImprovedVanillaConfig.MOB_DROP.mobSpawnEggDropChance.get();
        final Entity source = event.getSource().getTrueSource();
        final BlockPos pos = entity.getPosition();

        // killed by player and onlyWhenKilledByPlayer is true
        if ((source instanceof EntityPlayerMP) && onlyWhenKilledByPlayer) {
            // drop only when entity was killed by a player
            final EntityPlayerMP player = (EntityPlayerMP) source;
            if (player.isSpectator()) {
                return;
            }
            handleKilledByPlayer(world, event, entity, dropchance);

            // if allowed to drop always (onlyWhenKilledByPlayer == true)
        } else if (!onlyWhenKilledByPlayer) {

            // if the mob was killed by player anyway
            if (source instanceof EntityPlayerMP) {

                final EntityPlayerMP player = (EntityPlayerMP) source;
                if (player.isSpectator()) {
                    return;
                }
                handleKilledByPlayer(world, event, entity, dropchance);

                // not killed by player
            } else {
                if (Math.random() < ((double) dropchance / 100)) {
                    NBTTagCompound compound = entity.serializeNBT();
                    ItemStack spawnEgg = getMonsterEgg(compound.getString("id"));
                    spawnEgg.setCount(1);
                    EntityItem itemEntity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), spawnEgg);
                    event.getDrops().add(itemEntity);
                }

            }
        }
    }

    private static ItemStack getMonsterEgg(String resourceLocation) {
        // Create ItemStack with unspecified Spawn Egg
        ItemStack itemStack = new ItemStack(Items.SPAWN_EGG);

        // Do some NBT work to specify entity type
        NBTTagCompound nbttagcompound = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setString("id", resourceLocation);
        nbttagcompound.setTag("EntityTag", nbttagcompound1);

        // Set new NBT Data to Item Stack
        itemStack.setTagCompound(nbttagcompound);

        return itemStack;
    }

    private static void handleKilledByPlayer(World world, LivingDropsEvent event, EntityLivingBase entity, int dropchance) {
        final int lootingLevel = event.getLootingLevel();
        final boolean lootingAffective = ImprovedVanillaConfig.MOB_DROP.lootingAffective.get();
        final BlockPos pos = entity.getPosition();

        int count = 0;
        if (lootingAffective && lootingLevel >= 1) {
            // foreach lootinglevel there's an additional chance to drop the egg
            for (int i = 0; i < (1 + lootingLevel); i++) {
                if (Math.random() < ((double) dropchance / 100)) {
                    count++;
                }
            }
        } else {
            if (Math.random() < ((double) dropchance / 100)) {
                count++;
            }
        }
        if (count > 0) {
            NBTTagCompound compound = entity.serializeNBT();
            ItemStack spawnEgg = getMonsterEgg(compound.getString("id"));
            spawnEgg.setCount(count);
            EntityItem itemEntity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), spawnEgg);
            event.getDrops().add(itemEntity);
        }
    }

}