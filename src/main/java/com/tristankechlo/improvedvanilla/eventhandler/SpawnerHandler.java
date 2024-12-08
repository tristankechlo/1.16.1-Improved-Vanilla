package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class SpawnerHandler {

    @SubscribeEvent
    public void onSpawnerPlaced(final BlockEvent.NeighborNotifyEvent event) {
        final IWorld iWorld = event.getWorld();
        final BlockPos pos = event.getPos();

        if (iWorld == null || iWorld.isClientSide() || !(iWorld instanceof World)) {
            return;
        }
        if (!ImprovedVanillaConfig.SPAWNER.clearSpawner.get()) {
            return;
        }

        final World level = (World) iWorld;
        final Block targetBlock = level.getBlockState(pos).getBlock();

        if (targetBlock == Blocks.SPAWNER) {
            level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
            TileEntity tileEntity = level.getBlockEntity(pos);
            ((MobSpawnerTileEntity) tileEntity).getSpawner().setEntityId(EntityType.AREA_EFFECT_CLOUD);
            tileEntity.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        }
    }

    @SubscribeEvent
    public void onBlockBreakEvent(final BlockEvent.BreakEvent event) {
        final PlayerEntity player = event.getPlayer();
        final Block targetBlock = event.getState().getBlock();
        final IWorld iWorld = event.getWorld();
        final BlockPos pos = event.getPos();

        if (iWorld.isClientSide() || !(iWorld instanceof World)) {
            return;
        }
        final World world = (World) iWorld;
        if (targetBlock != Blocks.SPAWNER) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof PickaxeItem)) {
            event.setExpToDrop(0);
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        final int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player.getMainHandItem());
        final int silkTouchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, player.getMainHandItem());

        if (silkTouchLevel >= 1) {
            event.setExpToDrop(0);

            // try dropping the spawner itself
            final int spawnerDropChance = ImprovedVanillaConfig.SPAWNER.spawnerDropChance.get();
            if (spawnerDropChance >= 1 && spawnerDropChance <= 100) {
                if (Math.random() < ((double) spawnerDropChance / 100)) {
                    ItemStack stack = new ItemStack(Items.SPAWNER, 1);
                    ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    entity.setDefaultPickUpDelay();
                    world.addFreshEntity(entity);
                }
            } else {
                int exp = event.getExpToDrop();
                exp += (exp + 1) * world.getRandom().nextInt(4) * world.getRandom().nextInt(4);
                event.setExpToDrop(exp);
            }

            // try dropping the monster egg
            final int eggDropChance = ImprovedVanillaConfig.SPAWNER.spawnEggDropChance.get();
            if (eggDropChance >= 1 && eggDropChance <= 100) {
                if (Math.random() < ((double) eggDropChance / 100)) {
                    dropMonsterEggs(world, pos);
                }
            }

            // if other mods prevent the block break, at least the spawner is disabled
            resetSpawner(world, pos);
        } else if (silkTouchLevel == 0 && fortuneLevel >= 1) {
            int exp = event.getExpToDrop();
            exp += (exp + 1) * world.getRandom().nextInt(fortuneLevel) * world.getRandom().nextInt(fortuneLevel);
            event.setExpToDrop(exp);
        }
    }

    private static void resetSpawner(final World world, final BlockPos pos) {
        // remove old block-entity
        world.removeBlockEntity(pos);

        // create new block with new block-entity
        world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        MobSpawnerTileEntity tile = (MobSpawnerTileEntity) world.getBlockEntity(pos);

        CompoundNBT entity = new CompoundNBT();
        entity.putString("id", "minecraft:area_effect_cloud");

        CompoundNBT nbt = new CompoundNBT();
        nbt.put("Entity", entity);
        nbt.putInt("Weight", 1);

        WeightedSpawnerEntity nextSpawnData = new WeightedSpawnerEntity(nbt);
        tile.getSpawner().setNextSpawnData(nextSpawnData);
        tile.getSpawner().setEntityId(EntityType.AREA_EFFECT_CLOUD);
        tile.setChanged();
        world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    private static void dropMonsterEggs(final World world, final BlockPos pos) {
        Item item = getEggFromSpawner(world, pos);
        if (item == Items.AIR) {
            return;
        }
        ItemStack stack = new ItemStack(item);
        ItemEntity entityItem = new ItemEntity(world, pos.getX(), (pos.getY() + 1.0f), pos.getZ(), stack);
        entityItem.setDefaultPickUpDelay();
        world.addFreshEntity(entityItem);
    }

    private static Item getEggFromSpawner(final World world, final BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof MobSpawnerTileEntity)) {
            return Items.AIR;
        }

        // load the state of the spawner into this nbt
        AbstractSpawner logic = ((MobSpawnerTileEntity) tile).getSpawner();
        CompoundNBT nbt = new CompoundNBT();
        nbt = logic.save(nbt);

        // get the displayed entity
        if (nbt.contains("SpawnData")) {
            WeightedSpawnerEntity spawnData = new WeightedSpawnerEntity(1, nbt.getCompound("SpawnData"));
            String id = spawnData.getTag().getString("id"); // should be the id of the entity
            ResourceLocation search = new ResourceLocation(id + "_spawn_egg");
            Item item = ForgeRegistries.ITEMS.getValue(search);
            if (item == null) {
                ImprovedVanilla.LOGGER.info("Did not find a spawn-egg for '{}', searched for '{}'", id, search);
                return Items.AIR;
            }
            return item;
        }
        return Items.AIR;
    }
}