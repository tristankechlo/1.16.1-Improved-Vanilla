package com.tristankechlo.improvedvanilla.eventhandler;

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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.NonNullList;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpawnerHandler {

    @SubscribeEvent
    public void onSpawnerPlaced(final BlockEvent.NeighborNotifyEvent event) {
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
    public void onBlockBreackEvent(final BlockEvent.BreakEvent event) {
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
                    final ItemStack stack = new ItemStack(Item.getItemFromBlock(Blocks.MOB_SPAWNER), 1);
                    final EntityItem entity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    world.spawnEntity(entity);
                }
            } else {
                int exp = event.getExpToDrop();
                exp += (exp + 1) * world.rand.nextInt(4) * world.rand.nextInt(4);
                event.setExpToDrop(exp);
            }

            int eggDropChance = ImprovedVanillaConfig.SPAWNER.spawnEggDropChance.get();
            this.dropMonsterEggs(world, pos, eggDropChance);

            // if other mods prevent the block break, atleast the spawner is disabled
            this.resetSpawner(world, pos);

        } else if (silkTouchLevel == 0 && fortuneLevel >= 1) {
            int exp = event.getExpToDrop();
            exp += (exp + 1) * world.rand.nextInt(fortuneLevel) * world.rand.nextInt(fortuneLevel);
            event.setExpToDrop(exp);
        }
    }

    private void resetSpawner(final World world, final BlockPos pos) {
        if (world.getBlockState(pos).getBlock().equals(Blocks.MOB_SPAWNER)) {
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
    }

    private void dropMonsterEggs(final World world, final BlockPos pos, int eggDropChance) {
        if (eggDropChance <= 0) {
            return;
        }
        if (world.getBlockState(pos).getBlock().equals(Blocks.MOB_SPAWNER)) {

            final NonNullList<ItemStack> inv = getInvFromSpawner(world, pos);

            if (eggDropChance > 100) {
                eggDropChance = 100;
            }

            for (ItemStack itemStack : inv) {

                Item item = itemStack.getItem();
                int amount = itemStack.getCount();

                if (item == Items.AIR || amount < 1) {
                    continue;
                }

                if (Math.random() < ((double) eggDropChance / 100)) {
                    final EntityItem entityItem = new EntityItem(world, pos.getX(), (pos.getY() + 1.0f), pos.getZ(), itemStack);
                    world.spawnEntity(entityItem);
                }
            }
        }
    }

    private NonNullList<ItemStack> getInvFromSpawner(final World world, final BlockPos pos) {

        if (world.getBlockState(pos).getBlock().equals(Blocks.MOB_SPAWNER)) {

            final TileEntity tile = world.getTileEntity(pos);
            if (!(tile instanceof TileEntityMobSpawner)) {
                return NonNullList.withSize(1, ItemStack.EMPTY);
            }
            final MobSpawnerBaseLogic logic = ((TileEntityMobSpawner) tile).getSpawnerBaseLogic();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt = logic.writeToNBT(nbt);

            if (nbt.hasKey("SpawnPotentials", 9)) {
                NBTTagList listnbt = nbt.getTagList("SpawnPotentials", 10);
                int min = Math.min(11, listnbt.tagCount());
                NonNullList<ItemStack> inv = NonNullList.withSize(min, ItemStack.EMPTY);

                for (int i = 0; i < min; ++i) {
                    NBTTagCompound entry = listnbt.getCompoundTagAt(i);
                    String entity = entry.getCompoundTag("Entity").toString();
                    entity = entity.substring(entity.indexOf("\"") + 1);
                    entity = entity.substring(0, entity.indexOf("\""));
                    int weight = entry.getShort("Weight");
                    if (entity.equalsIgnoreCase(EntityList.getKey(EntityAreaEffectCloud.class).toString())) {
                        continue;
                    }
                    final ItemStack itemStack = new ItemStack(Items.SPAWN_EGG, weight);
                    NBTTagCompound e = new NBTTagCompound();
                    e.setString("id", entity);
                    itemStack.setTagInfo("EntityTag", e);
                    inv.set(i, itemStack);
                }
                return inv;
            }
        }

        return NonNullList.withSize(1, ItemStack.EMPTY);
    }
}