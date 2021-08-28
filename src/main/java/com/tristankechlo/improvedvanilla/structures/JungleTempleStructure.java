package com.tristankechlo.improvedvanilla.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class JungleTempleStructure extends Structure<NoFeatureConfig> {

	public static final List<String> DEFAULT_BIOMES = getDefaultSpawnBiomes();

	public JungleTempleStructure(Codec<NoFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public IStartFactory<NoFeatureConfig> getStartFactory() {
		return JungleTempleStructure.Start::new;
	}

	@Override
	public Decoration step() {
		return GenerationStage.Decoration.SURFACE_STRUCTURES;
	}

	@Override
	protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeProvider biomeSource, long seed,
			SharedSeedRandom chunkRandom, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos,
			NoFeatureConfig featureConfig) {
		BlockPos centerOfChunk = new BlockPos(chunkX * 16, 0, chunkZ * 16);
		int landHeight = chunkGenerator.getFirstOccupiedHeight(centerOfChunk.getX(), centerOfChunk.getZ(),
				Heightmap.Type.WORLD_SURFACE_WG);
		IBlockReader columnOfBlocks = chunkGenerator.getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ());
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

	public static class Start extends StructureStart<NoFeatureConfig> {

		private static final ResourceLocation START_POOL = new ResourceLocation(ImprovedVanilla.MOD_ID,
				"jungle_temple/jungle_temple_start");

		public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ,
				MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
		}

		@Override
		public void generatePieces(DynamicRegistries dynamicRegistryManager, ChunkGenerator chunkGenerator,
				TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoFeatureConfig config) {

			int x = chunkX * 16;
			int z = chunkZ * 16;
			BlockPos centerPos = new BlockPos(x - this.random.nextInt(10), 0, z - this.random.nextInt(10));
			final int maxDepth = 5;
			final boolean placeAtHeightMap = true;
			final boolean intersecting = false;
			JigsawManager.addPieces(dynamicRegistryManager,
					new VillageConfig(() -> getJigsawPattern(dynamicRegistryManager), maxDepth),
					AbstractVillagePiece::new, chunkGenerator, templateManagerIn, centerPos, this.pieces, this.random,
					intersecting, placeAtHeightMap);
			// move pieces to fit into land
			this.pieces.forEach(piece -> piece.move(0, 1, 0));
//			this.pieces.forEach(piece -> piece.getBoundingBox().y0 -= 1);

			Vector3i structureCenter = this.pieces.get(0).getBoundingBox().getCenter();
			int xOffset = centerPos.getX() - structureCenter.getX();
			int zOffset = centerPos.getZ() - structureCenter.getZ();
			this.pieces.forEach(piece -> piece.move(xOffset, 0, zOffset));
			this.calculateBoundingBox();
		}

		@Override
		public void placeInChunk(ISeedReader seedReader, StructureManager manager, ChunkGenerator chunkGenerator,
				Random random, MutableBoundingBox boundingBox, ChunkPos pos) {
			super.placeInChunk(seedReader, manager, chunkGenerator, random, boundingBox, pos);

			// place stone bricks under the structure
			int i = this.boundingBox.y0;
			for (int j = boundingBox.x0; j <= boundingBox.x1; ++j) {
				for (int k = boundingBox.z0; k <= boundingBox.z1; ++k) {
					BlockPos blockpos = new BlockPos(j, i, k);
					if (!seedReader.isEmptyBlock(blockpos) && this.boundingBox.isInside(blockpos)) {
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
								if (!seedReader.isEmptyBlock(blockpos1)
										&& !seedReader.getBlockState(blockpos1).getMaterial().isLiquid()) {
									break;
								}

								if (random.nextFloat() < 0.3F) {
									seedReader.setBlock(blockpos1, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
								} else {
									seedReader.setBlock(blockpos1, Blocks.STONE_BRICKS.defaultBlockState(), 2);
								}
							}
						}
					}
				}
			}
		}

		private JigsawPattern getJigsawPattern(DynamicRegistries dynamicRegistryManager) {
			return dynamicRegistryManager.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY).get(START_POOL);
		}

	}

}
