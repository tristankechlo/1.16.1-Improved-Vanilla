package com.tristankechlo.improvedvanilla.eventhandler;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
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
import java.util.function.Consumer;

public final class SpawnerHandler {

    public static InteractionResult onSpawnerPlaced(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        final Block targetBlock = level.getBlockState(pos).getBlock();
        if (targetBlock == Blocks.SPAWNER) {
            level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
            BlockEntity tileEntity = level.getBlockEntity(pos);
            ((SpawnerBlockEntity) tileEntity).getSpawner().setEntityId(EntityType.AREA_EFFECT_CLOUD);
            tileEntity.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        }
        return InteractionResult.SUCCESS;
    }

    //false to cancel, true to continue
    public static boolean onSpawnerBreak(Level level, Player player, BlockPos pos, BlockState state, int xpToDrop, Consumer<Integer> setExpToDrop) {
        if (level.isClientSide()) {
            return true;
        }
        final Block targetBlock = state.getBlock();
        if (targetBlock != Blocks.SPAWNER) {
            return true;
        }
        if (!(player.getMainHandItem().getItem() instanceof PickaxeItem)) {
            setExpToDrop.accept(0);
            return true;
        }
        if (player.isCreative() || player.isSpectator()) {
            return true;
        }

        final int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player.getMainHandItem());
        final int silkTouchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, player.getMainHandItem());

        if (silkTouchLevel >= 1) {
            setExpToDrop.accept(0);
            final int spawnerDropChance = 100 /*TODO ImprovedVanillaConfig.SERVER.spawnerDropChance.get()*/;
            if (spawnerDropChance >= 1 && spawnerDropChance <= 100) {
                if (Math.random() < ((double) spawnerDropChance / 100)) {
                    final ItemStack stack = new ItemStack(Items.SPAWNER, 1);
                    final ItemEntity entity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    level.addFreshEntity(entity);
                }
            } else {
                int exp = xpToDrop;
                exp += (exp + 1) * level.getRandom().nextInt(4) * level.getRandom().nextInt(4);
                setExpToDrop.accept(exp);
            }
            int eggDropChance = 100 /*TODO ImprovedVanillaConfig.SERVER.spawnEggDropChanceOnSpawnerDestroyed.get()*/;
            dropMonsterEggs(level, pos, eggDropChance);
            // if other mods prevent the block break, at least the spawner is disabled
            resetSpawner(level, pos);
        } else if (silkTouchLevel == 0 && fortuneLevel >= 1) {
            int exp = xpToDrop;
            exp += (exp + 1) * level.getRandom().nextInt(fortuneLevel) * level.getRandom().nextInt(fortuneLevel);
            setExpToDrop.accept(exp);
        }
        return true;
    }

    private static void resetSpawner(final Level world, final BlockPos pos) {
        if (world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
            world.removeBlockEntity(pos);
            world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
            SpawnerBlockEntity tile = (SpawnerBlockEntity) world.getBlockEntity(pos);

            final SpawnData nextSpawnData = new SpawnData(Util.make(new CompoundTag(), (ntb) -> {
                ntb.putString("id", Registry.ENTITY_TYPE.getKey(EntityType.AREA_EFFECT_CLOUD).toString());
            }), Optional.empty());
            tile.getSpawner().setNextSpawnData(world, pos, nextSpawnData);

            tile.setChanged();
            world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    private static void dropMonsterEggs(final Level world, final BlockPos pos, int eggDropChance) {
        if (eggDropChance <= 0) {
            return;
        }
        if (world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
            final SimpleContainer inv = getInvfromSpawner(world, pos);
            if (eggDropChance > 100) {
                eggDropChance = 100;
            }
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i) == ItemStack.EMPTY) {
                    continue;
                }
                Item item = inv.getItem(i).getItem();
                int weight = inv.getItem(i).getCount();
                if (item == Items.AIR || weight < 1) {
                    continue;
                }
                if (Math.random() < ((double) eggDropChance / 100)) {
                    final ItemEntity entityItem = new ItemEntity(world, pos.getX(), (pos.getY() + 1.0f), pos.getZ(), inv.getItem(i));
                    world.addFreshEntity(entityItem);
                }
            }
        }
    }

    private static SimpleContainer getInvfromSpawner(final Level world, final BlockPos pos) {
        if (!world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
            return new SimpleContainer(ItemStack.EMPTY);
        }
        final BlockEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof SpawnerBlockEntity)) {
            return new SimpleContainer(ItemStack.EMPTY);
        }

        final BaseSpawner logic = ((SpawnerBlockEntity) tile).getSpawner();
        CompoundTag nbt = new CompoundTag();
        nbt = logic.save(nbt);

        if (!nbt.contains("SpawnPotentials", 9) && !nbt.contains("SpawnData")) {
            return new SimpleContainer(ItemStack.EMPTY);
        }

        ListTag listnbt = nbt.getList("SpawnPotentials", 10);
        if (listnbt.size() > 0) {
            SimpleContainer inv = new SimpleContainer(listnbt.size());

            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundTag entry = listnbt.getCompound(i);
                String entity = entry.getCompound("data").getCompound("entity").toString();
                entity = entity.substring(entity.indexOf("\"") + 1);
                entity = entity.substring(0, entity.indexOf("\""));
                int weight = entry.getShort("weight");
                if (entity.equalsIgnoreCase(Registry.ENTITY_TYPE.getKey(EntityType.AREA_EFFECT_CLOUD).toString())) {
                    continue;
                }
                final ItemStack itemStack = new ItemStack(Registry.ITEM.get(new ResourceLocation(entity + "_spawn_egg")), weight);
                inv.setItem(i, itemStack);
            }
            return inv;
        }

        CompoundTag data = nbt.getCompound("SpawnData");
        if (data.contains("entity")) {
            String entity = data.getCompound("entity").toString();
            entity = entity.substring(entity.indexOf("\"") + 1);
            entity = entity.substring(0, entity.indexOf("\""));
            if (!entity.equalsIgnoreCase(Registry.ENTITY_TYPE.getKey(EntityType.AREA_EFFECT_CLOUD).toString())) {
                final ItemStack itemStack = new ItemStack(Registry.ITEM.get(new ResourceLocation(entity + "_spawn_egg")), 1);
                return new SimpleContainer(itemStack);
            }
        }
        return new SimpleContainer(ItemStack.EMPTY);
    }

}
