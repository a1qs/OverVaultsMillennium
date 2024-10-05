package io.iridium.overvaults.millenium.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockEntityChunkSavedData extends SavedData {
    private static final String DATA_NAME = "overvault_persisting_data";
    private final List<BlockPos> portalTilePositions = new ArrayList<>();
    private final Set<ChunkPos> loadedChunks = new HashSet<>();

    public static BlockEntityChunkSavedData load(CompoundTag nbt) {
        BlockEntityChunkSavedData data = new BlockEntityChunkSavedData();

        // Load portal tile positions
        ListTag portalPosList = nbt.getList("PortalTilePositions", Tag.TAG_COMPOUND);
        for (int i = 0; i < portalPosList.size(); i++) {
            CompoundTag dataTag = portalPosList.getCompound(i);
            BlockPos pos = BlockPos.of(dataTag.getLong("Pos"));
            data.addPortalTileEntity(pos);
        }

        // Load force-loaded chunks
        ListTag chunkList = nbt.getList("LoadedChunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < chunkList.size(); i++) {
            CompoundTag dataTag = chunkList.getCompound(i);
            int chunkX = dataTag.getInt("ChunkX");
            int chunkZ = dataTag.getInt("ChunkZ");
            data.addForceloadedChunk(chunkX, chunkZ);
        }

        return data;
    }


    @Override
    public CompoundTag save(CompoundTag nbt) {
        // Save portal tile positions
        ListTag portalPosList = new ListTag();
        for (BlockPos pos : portalTilePositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            portalPosList.add(posTag);
        }
        nbt.put("PortalTilePositions", portalPosList);

        ListTag chunkList = new ListTag();
        for (ChunkPos chunkPos : loadedChunks) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putInt("ChunkX", chunkPos.x);
            chunkTag.putInt("ChunkZ", chunkPos.z);
            chunkList.add(chunkTag);
        }
        nbt.put("LoadedChunks", chunkList);
        return nbt;
    }


    public void addPortalTileEntity(BlockPos pos) {
        portalTilePositions.add(pos);
        setDirty();
    }

    public void addForceloadedChunk(int chunkX, int chunkZ) {
        loadedChunks.add(new ChunkPos(chunkX, chunkZ));
        setDirty();
    }

    public void removePortalTileEntityData() {
        portalTilePositions.clear();
        setDirty();
    }

    public void removeChunkPositionData() {
        loadedChunks.clear();
        setDirty();
    }

    public List<BlockPos> getPortalTilePositions() {
        return portalTilePositions;
    }

    public Set<ChunkPos> getForceloadedChunks() {
        return loadedChunks;
    }



    public static BlockEntityChunkSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BlockEntityChunkSavedData::load, BlockEntityChunkSavedData::new, DATA_NAME);
    }
}