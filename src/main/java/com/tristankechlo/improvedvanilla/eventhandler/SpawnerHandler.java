package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber
public class SpawnerHandler {

    @SubscribeEvent
    public void onSpawnerPlaced(final BlockEvent.NeighborNotifyEvent event) {
        final LevelAccessor world = event.getWorld();
        final BlockPos pos = event.getPos();

        if (world == null || world.isClientSide() || !(world instanceof Level level)) {
            return;
        }
        if (!ImprovedVanillaConfig.SPAWNER.clearSpawner.get()) {
            return;
        }

        final Block targetBlock = level.getBlockState(pos).getBlock();

        if (targetBlock == Blocks.SPAWNER) {
            level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
            BlockEntity tileEntity = level.getBlockEntity(pos);
            ((SpawnerBlockEntity) tileEntity).getSpawner().setEntityId(EntityType.AREA_EFFECT_CLOUD);
            tileEntity.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        }
    }

    @SubscribeEvent
    public void onBlockBreakEvent(final BlockEvent.BreakEvent event) {
        final Player player = event.getPlayer();
        final Block targetBlock = event.getState().getBlock();
        final LevelAccessor world = event.getWorld();
        final BlockPos pos = event.getPos();

        if (world.isClientSide() || !(world instanceof Level level) || targetBlock != Blocks.SPAWNER) {
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
                    ImprovedVanilla.dropItemStackInWorld(level, pos, stack);
                }
            } else {
                int exp = event.getExpToDrop();
                exp += (exp + 1) * level.getRandom().nextInt(4) * level.getRandom().nextInt(4);
                event.setExpToDrop(exp);
            }

            // try dropping the monster egg
            final int eggDropChance = ImprovedVanillaConfig.SPAWNER.spawnEggDropChance.get();
            if (eggDropChance >= 1 && eggDropChance <= 100) {
                if (Math.random() < ((double) eggDropChance / 100)) {
                    dropMonsterEggs(level, pos);
                }
            }

            // if other mods prevent the block break, at least the spawner is disabled
            resetSpawner(level, pos);
        } else if (silkTouchLevel == 0 && fortuneLevel >= 1) {
            int exp = event.getExpToDrop();
            exp += (exp + 1) * level.getRandom().nextInt(fortuneLevel) * level.getRandom().nextInt(fortuneLevel);
            event.setExpToDrop(exp);
        }
    }

    private static void resetSpawner(final Level world, final BlockPos pos) {
        // remove old block-entity
        world.removeBlockEntity(pos);

        // create new block with new block-entity
        world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        SpawnerBlockEntity tile = (SpawnerBlockEntity) world.getBlockEntity(pos);

        CompoundTag entity = new CompoundTag();
        entity.putString("id", "minecraft:area_effect_cloud");

        SpawnData nextSpawnData = new SpawnData(entity, Optional.empty());
        tile.getSpawner().setNextSpawnData(world, pos, nextSpawnData);
        tile.getSpawner().setEntityId(EntityType.AREA_EFFECT_CLOUD);
        tile.setChanged();
        world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    private static void dropMonsterEggs(final Level world, final BlockPos pos) {
        ItemStack stack = getEggFromSpawner(world, pos);
        ImprovedVanilla.dropItemStackInWorld(world, pos, stack);
    }

    private static ItemStack getEggFromSpawner(final Level world, final BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof SpawnerBlockEntity)) {
            return ItemStack.EMPTY;
        }

        // load the state of the spawner into this nbt
        BaseSpawner logic = ((SpawnerBlockEntity) tile).getSpawner();
        CompoundTag nbt = new CompoundTag();
        nbt = logic.save(nbt);

        // get the displayed entity
        if (nbt.contains("SpawnData")) {
            SpawnData spawnData = new SpawnData(nbt.getCompound("SpawnData").getCompound("entity"), Optional.empty());
            String id = spawnData.entityToSpawn().getString("id"); // should be the id of the entity
            return ImprovedVanilla.getMonsterEgg(id, 1);
        }
        return ItemStack.EMPTY;
    }
}
