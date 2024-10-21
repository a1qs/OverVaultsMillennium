package io.iridium.overvaults.millenium.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public class PortalSavedData extends SavedData {
    private static final String DATA_NAME = "overvaults_stored_structures";
    private final List<PortalData> portalDataList = new ArrayList<>();

    public void addPortalData(PortalData newPortalData) {
        // Check if the new portal data is already in the list
        boolean exists = portalDataList.stream().anyMatch(existingPortalData -> existingPortalData.equals(newPortalData));

        if (!exists) {
            portalDataList.add(newPortalData);
            setDirty();
        }
    }

    public List<PortalData> getPortalData() {
        return portalDataList;
    }

    public void removePortalData(int index) {
        portalDataList.remove(index);
        setDirty();
    }

    public boolean hasActiveOverVault() {
        return portalDataList.stream().anyMatch(PortalData::getActiveState);
    }


    public PortalData getFirstActivePortalData() {
        return portalDataList.stream()
                .filter(PortalData::getActiveState)
                .findFirst()
                .orElse(null);
    }

    public static PortalSavedData load(CompoundTag nbt) {
        PortalSavedData data = new PortalSavedData();
        ListTag listTag = nbt.getList("PortalDataList", Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag portalTag = listTag.getCompound(i);

            Rotation rotation = Rotation.valueOf(portalTag.getString("Rotation"));
            BlockPos portalFrameCenterPos = BlockPos.of(portalTag.getLong("PortalFrameCenterPos"));
            StructureSize size = StructureSize.valueOf(portalTag.getString("Size"));
            ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(portalTag.getString("Dimension")));
            boolean activeState = portalTag.getBoolean("ActiveState");

            data.addPortalData(new PortalData(rotation, portalFrameCenterPos, size, dimension, activeState));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag listTag = new ListTag();

        for (PortalData data : portalDataList) {
            CompoundTag portalTag = new CompoundTag();
            portalTag.putString("Rotation", data.getRotation().name());
            portalTag.putLong("PortalFrameCenterPos", data.getPortalFrameCenterPos().asLong());
            portalTag.putString("Size", data.getSize().name());
            portalTag.putString("Dimension", data.getDimension().location().toString());
            portalTag.putBoolean("ActiveState", data.getActiveState());

            listTag.add(portalTag);
        }
        nbt.put("PortalDataList", listTag);
        return nbt;
    }

    public static PortalSavedData get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(PortalSavedData::load, PortalSavedData::new, DATA_NAME);
    }
}
