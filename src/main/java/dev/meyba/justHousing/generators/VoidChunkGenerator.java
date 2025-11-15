package dev.meyba.justHousing.generators;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class VoidChunkGenerator extends ChunkGenerator {
    private final int borderSize;

    public VoidChunkGenerator(int borderSize) {
        this.borderSize = borderSize;
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;

        int radius = borderSize / 2;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                if (worldX >= -radius && worldX < radius && worldZ >= -radius && worldZ < radius) {
                    chunkData.setBlock(x, 0, z, Material.BEDROCK);
                    chunkData.setBlock(x, 1, z, Material.GRASS_BLOCK);
                }
            }
        }
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
}