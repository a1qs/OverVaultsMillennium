package io.iridium.overvaults.millenium.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

public class PortalData {
    private final Rotation rotation;
    private final BlockPos portalFrameCenterPos;
    private final StructureSize size;
    private final ResourceKey<Level> dimension;
    private boolean activated;


    public PortalData(Rotation rotation, BlockPos portalFrameCenterPos, StructureSize size, ResourceKey<Level> dimension, boolean activated) {
        this.rotation = rotation;
        this.portalFrameCenterPos = portalFrameCenterPos;
        this.size = size;
        this.activated = activated;
        this.dimension = dimension;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public BlockPos getPortalFrameCenterPos() {
        return portalFrameCenterPos;
    }

    public StructureSize getSize() {
        return size;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public boolean getActiveState() {
        return activated;
    }

    public void setActiveState(boolean state) {
        activated = state;
    }

    //Portals that have different Active States will be considered equal
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        PortalData data = (PortalData) obj;

        if (!rotation.equals(data.rotation)) return false;
        if (!portalFrameCenterPos.equals(data.portalFrameCenterPos)) return false;
        if (!size.equals(data.size)) return false;
        return dimension.equals(data.dimension);
    }

    @Override
    public int hashCode() {
        int result = rotation.hashCode();
        result = 31 * result + portalFrameCenterPos.hashCode();
        result = 31 * result + size.hashCode();
        result = 31 * result + dimension.hashCode();
        return result;
    }
}
