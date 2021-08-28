package com.tristankechlo.improvedvanilla.structures;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;

import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
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
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class ForgottenWellStructure extends Structure<NoFeatureConfig> {

	public static final List<String> DEFAULT_BIOMES = getDefaultSpawnBiomes();

	public ForgottenWellStructure(Codec<NoFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public IStartFactory<NoFeatureConfig> getStartFactory() {
		return ForgottenWellStructure.Start::new;
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

	public static class Start extends StructureStart<NoFeatureConfig> {

		public static final ResourceLocation START_POOL = new ResourceLocation(ImprovedVanilla.MOD_ID,
				"forgotten_well_start_pool");

		public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ,
				MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
		}

		@Override
		public void generatePieces(DynamicRegistries dynamicRegistryManager, ChunkGenerator chunkGenerator,
				TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoFeatureConfig config) {

			int x = chunkX * 16;
			int z = chunkZ * 16;
			BlockPos centerPos = new BlockPos(x, 0, z);
			final int maxDepth = 10;
			final boolean placeAtHeightMap = true;
			final boolean intersecting = false;
			JigsawManager.addPieces(dynamicRegistryManager,
					new VillageConfig(() -> getJigsawPattern(dynamicRegistryManager), maxDepth),
					AbstractVillagePiece::new, chunkGenerator, templateManagerIn, centerPos, this.pieces, this.random,
					intersecting, placeAtHeightMap);
			// move pieces to fit into land
			this.pieces.forEach(piece -> piece.move(0, -2, 0));
			this.pieces.forEach(piece -> piece.getBoundingBox().y0 += 2);

			Vector3i structureCenter = this.pieces.get(0).getBoundingBox().getCenter();
			int xOffset = centerPos.getX() - structureCenter.getX();
			int zOffset = centerPos.getZ() - structureCenter.getZ();
			for (StructurePiece structurePiece : this.pieces) {
				structurePiece.move(xOffset, 0, zOffset);
			}
			this.calculateBoundingBox();
		}

		private JigsawPattern getJigsawPattern(DynamicRegistries dynamicRegistryManager) {
			return dynamicRegistryManager.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY).get(START_POOL);
		}

	}

}
