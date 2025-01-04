package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import com.tristankechlo.improvedvanilla.platform.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class SpawnerHandler {

    public static void onSpawnerBreak(Level level, Player player, BlockPos pos, BlockState state) {
        final Block targetBlock = state.getBlock();

        if (level.isClientSide() || targetBlock != Blocks.SPAWNER) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof PickaxeItem)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        Registry<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        final int silkTouchLevel = EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(Enchantments.SILK_TOUCH), player.getMainHandItem());

        if (silkTouchLevel >= 1) {

            // try dropping the spawner itself
            final int spawnerDropChance = ImprovedVanillaConfig.get().spawner().spawnerDropChance();
            if (spawnerDropChance >= 1 && spawnerDropChance <= 100) {
                if (Math.random() < ((double) spawnerDropChance / 100)) {
                    ItemStack stack = new ItemStack(Items.SPAWNER, 1);
                    ImprovedVanilla.dropItemStackInWorld(level, pos, stack);
                }
            }

            // try dropping the monster egg
            final int eggDropChance = ImprovedVanillaConfig.get().spawner().spawnEggDropChance();
            if (eggDropChance >= 1 && eggDropChance <= 100) {
                if (Math.random() < ((double) eggDropChance / 100)) {
                    dropMonsterEggs(level, pos);
                }
            }

            // if other mods prevent the block break, at least the spawner is disabled
            resetSpawner(level, pos);
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

        SpawnData nextSpawnData = new SpawnData(entity, Optional.empty(), Optional.empty());
        IPlatformHelper.INSTANCE.setNextSpawnData(tile.getSpawner(), world, pos, nextSpawnData);
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
            SpawnData spawnData = new SpawnData(nbt.getCompound("SpawnData").getCompound("entity"), Optional.empty(), Optional.empty());
            String id = spawnData.entityToSpawn().getString("id"); // should be the id of the entity
            if (id.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return ImprovedVanilla.getMonsterEgg(id, 1);
        }
        return ItemStack.EMPTY;
    }
}
