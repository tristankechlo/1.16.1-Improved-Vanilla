package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpawnerHandler {

    @SubscribeEvent
    public void onSpawnerPlaced(BlockEvent.NeighborNotifyEvent event) {
        final World world = event.getWorld();
        final BlockPos pos = event.getPos();

        if (world.isRemote) {
            return;
        }
        if (!ImprovedVanillaConfig.SPAWNER.clearSpawner.get()) {
            return;
        }

        final Block targetblock = world.getBlockState(pos).getBlock();
        if (targetblock instanceof BlockMobSpawner) {
            world.setBlockState(pos, Blocks.MOB_SPAWNER.getDefaultState(), 2);
            TileEntity tileentity = world.getTileEntity(pos);
            ((TileEntityMobSpawner) tileentity).getSpawnerBaseLogic().setEntityId(EntityList.getKey(EntityAreaEffectCloud.class));
            tileentity.markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        final EntityPlayer player = event.getPlayer();
        final Block targetBlock = event.getState().getBlock();
        final World world = event.getWorld();
        final BlockPos pos = event.getPos();

        if (world.isRemote) {
            return;
        }
        if (targetBlock != Blocks.MOB_SPAWNER) {
            return;
        }
        if (!(player.getHeldItemMainhand().getItem() instanceof ItemPickaxe)) {
            event.setExpToDrop(0);
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        final int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, player.getHeldItemMainhand());
        final int silkTouchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand());

        if (silkTouchLevel >= 1) {
            event.setExpToDrop(0);
            final int spawnerDropChance = ImprovedVanillaConfig.SPAWNER.spawnerDropChance.get();

            if (spawnerDropChance >= 1 && spawnerDropChance <= 100) {
                if (Math.random() < ((double) spawnerDropChance / 100)) {
                    ItemStack stack = new ItemStack(Item.getItemFromBlock(Blocks.MOB_SPAWNER), 1);
                    EntityItem entity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    entity.setDefaultPickupDelay();
                    world.spawnEntity(entity);
                }
            } else {
                int exp = event.getExpToDrop();
                exp += (exp + 1) * world.rand.nextInt(4) * world.rand.nextInt(4);
                event.setExpToDrop(exp);
            }

            // try dropping the monster egg
            int eggDropChance = ImprovedVanillaConfig.SPAWNER.spawnEggDropChance.get();
            if (eggDropChance >= 1 && eggDropChance <= 100) {
                if (Math.random() < ((double) eggDropChance / 100)) {
                    dropMonsterEggs(world, pos);
                }
            }

            // if other mods prevent the block break, atleast the spawner is disabled
            resetSpawner(world, pos);
        } else if (silkTouchLevel == 0 && fortuneLevel >= 1) {
            int exp = event.getExpToDrop();
            exp += (exp + 1) * world.rand.nextInt(fortuneLevel) * world.rand.nextInt(fortuneLevel);
            event.setExpToDrop(exp);
        }
    }

    private static void resetSpawner(World world, BlockPos pos) {
        world.removeTileEntity(pos);
        world.setBlockState(pos, Blocks.MOB_SPAWNER.getDefaultState(), 2);
        TileEntityMobSpawner tile = (TileEntityMobSpawner) world.getTileEntity(pos);

        NBTTagCompound entity = new NBTTagCompound();
        entity.setString("id", "minecraft:area_effect_cloud");

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Entity", entity);
        nbt.setInteger("Weight", 1);

        final WeightedSpawnerEntity nextSpawnData = new WeightedSpawnerEntity(nbt);
        tile.getSpawnerBaseLogic().setNextSpawnData(nextSpawnData);
        tile.getSpawnerBaseLogic().setEntityId(EntityList.getKey(EntityAreaEffectCloud.class));
        tile.markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    private static void dropMonsterEggs(World world, BlockPos pos) {
        ItemStack stack = getEggFromSpawner(world, pos);
        if (stack.isEmpty()) {
            return;
        }
        EntityItem entityItem = new EntityItem(world, pos.getX(), (pos.getY() + 1.0f), pos.getZ(), stack);
        entityItem.setDefaultPickupDelay();
        world.spawnEntity(entityItem);
    }

    private static ItemStack getEggFromSpawner(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityMobSpawner)) {
            return ItemStack.EMPTY;
        }

        // load the state of the spawner into this nbt
        MobSpawnerBaseLogic logic = ((TileEntityMobSpawner) tile).getSpawnerBaseLogic();
        NBTTagCompound nbt = new NBTTagCompound();
        nbt = logic.writeToNBT(nbt);

        // get the displayed entity
        if (nbt.hasKey("SpawnData")) {
            WeightedSpawnerEntity spawnData = new WeightedSpawnerEntity(1, nbt.getCompoundTag("SpawnData"));
            String id = spawnData.getNbt().getString("id"); // should be the id of the entity
            ItemStack stack = ImprovedVanilla.getMonsterEgg(id);
            if (stack.isEmpty()) {
                ImprovedVanilla.LOGGER.info("Did not find a spawn-egg for '{}'", id);
                return ItemStack.EMPTY;
            }
            return stack;
        }
        return ItemStack.EMPTY;
    }

}
