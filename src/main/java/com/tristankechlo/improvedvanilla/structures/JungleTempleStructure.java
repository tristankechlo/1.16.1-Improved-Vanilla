package com.tristankechlo.improvedvanilla.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JungleTempleStructure extends StructureFeature<NoneFeatureConfiguration> {

	public static final List<String> DEFAULT_BIOMES = getDefaultSpawnBiomes();

	public JungleTempleStructure(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return JungleTempleStructure.Start::new;
	}

	@Override
	public Decoration step() {
		return Decoration.SURFACE_STRUCTURES;
	}

	@Override
	protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long seed,
			WorldgenRandom random, ChunkPos chunkPos1, Biome biome, ChunkPos chunkPos2,
			NoneFeatureConfiguration featureConfig, LevelHeightAccessor heightLimitView) {
		BlockPos centerOfChunk = chunkPos1.getWorldPosition();
		int landHeight = chunkGenerator.getFirstOccupiedHeight(centerOfChunk.getX(), centerOfChunk.getZ(),
				Heightmap.Types.WORLD_SURFACE_WG, heightLimitView);
		NoiseColumn columnOfBlocks = chunkGenerator.getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ(),
				heightLimitView);
		BlockState topBlock = columnOfBlocks.getBlockState(centerOfChunk.above(landHeight));
		return topBlock.getFluidState().isEmpty() && landHeight <= 80;
	}

	private static List<String> getDefaultSpawnBiomes() {
		List<String> biomes = new ArrayList<>();
		biomes.add(Biomes.JUNGLE.location().toString());
		biomes.add(Biomes.JUNGLE_EDGE.location().toString());
		biomes.add(Biomes.JUNGLE_HILLS.location().toString());
		biomes.add(Biomes.MODIFIED_JUNGLE.location().toString());
		biomes.add(Biomes.MODIFIED_JUNGLE_EDGE.location().toString());
		biomes.add(Biomes.BAMBOO_JUNGLE.location().toString());
		biomes.add(Biomes.BAMBOO_JUNGLE_HILLS.location().toString());
		return biomes;
	}

	public static class Start extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {

		private static final ResourceLocation START_POOL = new ResourceLocation(ImprovedVanilla.MOD_ID,
				"jungle_temple/jungle_temple_start");

		public Start(StructureFeature<NoneFeatureConfiguration> structureIn, ChunkPos chunkPos, int referenceIn,
				long seedIn) {
			super(structureIn, chunkPos, referenceIn, seedIn);
		}

		@Override
		public void generatePieces(RegistryAccess dynamicRegistryAccess, ChunkGenerator chunkGenerator,
				StructureManager structureManager, ChunkPos chunkPos, Biome biomeIn, NoneFeatureConfiguration config,
				LevelHeightAccessor heightLimitView) {

			BlockPos structureBlockPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
			final int maxDepth = 5;
			final boolean placeAtHeightMap = true;
			final boolean intersecting = false;
			JigsawPlacement.addPieces(dynamicRegistryAccess,
					new JigsawConfiguration(() -> getJigsawPattern(dynamicRegistryAccess), maxDepth),
					PoolElementStructurePiece::new, chunkGenerator, structureManager, structureBlockPos, this,
					this.random, intersecting, placeAtHeightMap, heightLimitView);

			// move pieces to fit into land
			this.pieces.forEach(piece -> piece.move(0, 1, 0));

			// center at start pos
			Vec3i structureCenter = this.pieces.get(0).getBoundingBox().getCenter();
			int xOffset = structureBlockPos.getX() - structureCenter.getX();
			int zOffset = structureBlockPos.getZ() - structureCenter.getZ();
			this.pieces.forEach(piece -> piece.move(xOffset, 0, zOffset));
			this.getBoundingBox();
		}

		@Override
		public void placeInChunk(WorldGenLevel level, StructureFeatureManager manager, ChunkGenerator chunkGenerator,
				Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
			super.placeInChunk(level, manager, chunkGenerator, random, boundingBox, chunkPos);

			int i = this.getBoundingBox().minY();
			for (int j = boundingBox.minX(); j <= boundingBox.maxX(); ++j) {
				for (int k = boundingBox.minZ(); k <= boundingBox.maxZ(); ++k) {

					BlockPos blockpos = new BlockPos(j, i, k);
					if (!level.isEmptyBlock(blockpos) && this.getBoundingBox().isInside(blockpos)) {

						boolean flag = false;
						for (StructurePiece structurepiece : this.pieces) {
							if (structurepiece.getBoundingBox().isInside(blockpos)) {
								flag = true;
								break;
							}
						}

						if (flag) {
							for (int l = i - 1; l > 1; --l) {
								BlockPos blockpos1 = new BlockPos(j, l, k);
								if (level.isEmptyBlock(blockpos1) || level.getBlockState(blockpos1).getMaterial().isLiquid()) {
									break;
								}

								if (random.nextFloat() < 0.3F) {
									level.setBlock(blockpos1, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
								} else {
									level.setBlock(blockpos1, Blocks.STONE_BRICKS.defaultBlockState(), 2);
								}
							}
						}
					}
				}
			}

		}

		private StructureTemplatePool getJigsawPattern(RegistryAccess dynamicRegistryManager) {
			return dynamicRegistryManager.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY).get(START_POOL);
		}

	}

}