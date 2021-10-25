package com.tristankechlo.improvedvanilla.structures;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class UnderGroundTempleStructure extends StructureFeature<NoneFeatureConfiguration> {

	private static final List<SpawnerData> STRUCTURE_MONSTERS = ImmutableList.of(
			new SpawnerData(EntityType.ILLUSIONER, 100, 4, 9), new SpawnerData(EntityType.VINDICATOR, 100, 4, 9),
			new SpawnerData(EntityType.PILLAGER, 100, 4, 9));

	public UnderGroundTempleStructure(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public List<SpawnerData> getDefaultSpawnList() {
		return STRUCTURE_MONSTERS;
	}

	@Override
	public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return UnderGroundTempleStructure.Start::new;
	}

	@Override
	public Decoration step() {
		return Decoration.UNDERGROUND_STRUCTURES;
	}

	@Override
	protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long seed,
			WorldgenRandom random, ChunkPos chunkPos1, Biome biome, ChunkPos chunkPos2,
			NoneFeatureConfiguration featureConfig, LevelHeightAccessor heightLimitView) {
		return random.nextDouble() < 0.6;
	}

	public static class Start extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {

		private static final ResourceLocation START_POOL = new ResourceLocation(ImprovedVanilla.MOD_ID,
				"underground_temple/underground_temple_start");

		public Start(StructureFeature<NoneFeatureConfiguration> structureIn, ChunkPos chunkPos, int referenceIn,
				long seedIn) {
			super(structureIn, chunkPos, referenceIn, seedIn);
		}

		@Override
		public void generatePieces(RegistryAccess dynamicRegistryAccess, ChunkGenerator chunkGenerator,
				StructureManager structureManager, ChunkPos chunkPos, Biome biomeIn, NoneFeatureConfiguration config,
				LevelHeightAccessor heightLimitView) {

			BlockPos structureBlockPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
			final int maxDepth = 6;
			final boolean placeAtHeightMap = false;
			final boolean intersecting = false;
			JigsawPlacement.addPieces(dynamicRegistryAccess,
					new JigsawConfiguration(() -> getJigsawPattern(dynamicRegistryAccess), maxDepth),
					PoolElementStructurePiece::new, chunkGenerator, structureManager, structureBlockPos, this,
					this.random, intersecting, placeAtHeightMap, heightLimitView);

			Vec3i structureCenter = this.pieces.get(0).getBoundingBox().getCenter();
			int xOffset = structureBlockPos.getX() - structureCenter.getX();
			int zOffset = structureBlockPos.getZ() - structureCenter.getZ();
			this.pieces.forEach(piece -> piece.move(xOffset, 0, zOffset));
			this.getBoundingBox();
			this.moveBelowSeaLevel(chunkGenerator.getSeaLevel(), chunkGenerator.getMinY(), this.random, 20);
		}

		private StructureTemplatePool getJigsawPattern(RegistryAccess dynamicRegistryManager) {
			return dynamicRegistryManager.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY).get(START_POOL);
		}

	}

}