package com.tristankechlo.improvedvanilla.structures;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ForgottenWellStructure extends StructureFeature<NoneFeatureConfiguration> {

	public static final List<String> DEFAULT_BIOMES = getDefaultSpawnBiomes();

	public ForgottenWellStructure(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return ForgottenWellStructure.Start::new;
	}

	@Override
	public GenerationStep.Decoration step() {
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	@Override
	protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long seed,
			WorldgenRandom random, ChunkPos chunkPos1, Biome biome, ChunkPos chunkPos2,
			NoneFeatureConfiguration featureConfig, LevelHeightAccessor heightLimitView) {
		BlockPos blockPos = chunkPos1.getWorldPosition();
		int landHeight = chunkGenerator.getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(),
				Heightmap.Types.WORLD_SURFACE_WG, heightLimitView);
		NoiseColumn columnOfBlocks = chunkGenerator.getBaseColumn(blockPos.getX(), blockPos.getZ(), heightLimitView);
		BlockState topBlock = columnOfBlocks.getBlockState(blockPos.above(landHeight));
		return topBlock.getFluidState().isEmpty();
	}

	private static List<String> getDefaultSpawnBiomes() {
		List<String> biomes = new ArrayList<>();
		biomes.add(Biomes.FOREST.location().toString());
		biomes.add(Biomes.BIRCH_FOREST.location().toString());
		biomes.add(Biomes.BIRCH_FOREST_HILLS.location().toString());
		biomes.add(Biomes.DARK_FOREST.location().toString());
		biomes.add(Biomes.DARK_FOREST_HILLS.location().toString());
		biomes.add(Biomes.TALL_BIRCH_FOREST.location().toString());
		biomes.add(Biomes.FLOWER_FOREST.location().toString());
		return biomes;
	}

	public static class Start extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {

		public static final ResourceLocation START_POOL = new ResourceLocation(ImprovedVanilla.MOD_ID,
				"forgotten_well_start_pool");

		public Start(StructureFeature<NoneFeatureConfiguration> structureIn, ChunkPos chunkPos, int referenceIn,
				long seedIn) {
			super(structureIn, chunkPos, referenceIn, seedIn);
		}

		@Override
		public void generatePieces(RegistryAccess dynamicRegistryAccess, ChunkGenerator chunkGenerator,
				StructureManager structureManager, ChunkPos chunkPos, Biome biomeIn, NoneFeatureConfiguration config,
				LevelHeightAccessor heightLimitView) {

			BlockPos structureBlockPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
			final int maxDepth = 10;
			final boolean intersecting = false;
			final boolean placeAtHeightMap = true;
			JigsawPlacement.addPieces(dynamicRegistryAccess,
					new JigsawConfiguration(() -> getJigsawPattern(dynamicRegistryAccess), maxDepth),
					PoolElementStructurePiece::new, chunkGenerator, structureManager, structureBlockPos, this,
					this.random, intersecting, placeAtHeightMap, heightLimitView);

			// move pieces to fit into land
			this.pieces.forEach(piece -> piece.move(0, -2, 0));

			// center piece at start pos
			Vec3i structureCenter = this.pieces.get(0).getBoundingBox().getCenter();
			int xOffset = structureBlockPos.getX() - structureCenter.getX();
			int zOffset = structureBlockPos.getZ() - structureCenter.getZ();
			this.pieces.forEach(piece -> piece.move(xOffset, 0, zOffset));

			this.getBoundingBox();
		}

		private StructureTemplatePool getJigsawPattern(RegistryAccess dynamicRegistryManager) {
			return dynamicRegistryManager.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY).get(START_POOL);
		}

	}

}