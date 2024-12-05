package io.iridium.overvaults.config.vault;

import com.google.gson.annotations.Expose;
import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.config.vault.entry.EntityDataEntry;
import io.iridium.overvaults.millenium.world.PortalData;
import iskallia.vault.config.Config;
import iskallia.vault.util.data.WeightedList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class OverVaultsMobConfig extends Config {

    @Expose public int MODIFIERS_TO_REMOVE_UNTIL_TIER_UP;

    @Expose public EntityDataEntry TIER_0_ENTITY;
    @Expose public EntityDataEntry TIER_1_ENTITY;
    @Expose public EntityDataEntry TIER_2_ENTITY;
    @Expose public EntityDataEntry TIER_3_ENTITY;
    @Expose public EntityDataEntry TIER_4_ENTITY;

    @Expose public WeightedList<EntityDataEntry> BOSS_ENTRIES = new WeightedList<>();


    @Override
    public String getName() {
        return "~overvaults_mobs";
    }

    @Override
    protected void reset() {
        MODIFIERS_TO_REMOVE_UNTIL_TIER_UP = 5;
        TIER_0_ENTITY = new EntityDataEntry(new ResourceLocation("the_vault:vault_fighter_0"), 20.0F, 2.0F);
        TIER_1_ENTITY = new EntityDataEntry(new ResourceLocation("the_vault:vault_fighter_1"), 25.0F, 4.0F);
        TIER_2_ENTITY = new EntityDataEntry(new ResourceLocation("the_vault:vault_fighter_2"), 30.0F, 6.0F);
        TIER_3_ENTITY = new EntityDataEntry(new ResourceLocation("the_vault:vault_fighter_3"), 35.0F, 8.0F);
        TIER_4_ENTITY = new EntityDataEntry(new ResourceLocation("the_vault:vault_fighter_4"), 40.0F, 10.0F);

        BOSS_ENTRIES.add(new EntityDataEntry(new ResourceLocation("the_vault:boogieman"), 50.0F, 5.0F), 10);
        BOSS_ENTRIES.add(new EntityDataEntry(new ResourceLocation("the_vault:robot"), 75.0F, 5.0F), 5);
    }

    public void addPortalEntityToWorld(ServerLevel level, int step, PortalData portalData) {
        EntityDataEntry entityData = step == 5 ? BOSS_ENTRIES.getRandom(Config.rand) : getEntityForStep(step);
        if (entityData == null) {
            OverVaults.LOGGER.error("No entity data found for step {}", step);
            return;
        }

        EntityType<?> entityType = EntityType.byString(entityData.entityId.toString()).orElse(null);
        if (entityType == null) {
            OverVaults.LOGGER.error("Invalid entity type '{}' for step {}", entityData.entityId, step);
            return;
        }

        Entity entity = entityType.create(level);
        if (entity == null) {
            OverVaults.LOGGER.error("Failed to create entity for type '{}' at step {}", entityType, step);
            return;
        }

        BlockPos spawnPosition = findSpawnPosition(level, portalData);
        entity.moveTo(spawnPosition.getX() + 0.5, spawnPosition.getY() + 1.0, spawnPosition.getZ() + 0.5);

        if (entity instanceof LivingEntity livingEntity) {
            var healthAttribute = livingEntity.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.setBaseValue(entityData.entityHitpoints);
                livingEntity.setHealth(entityData.entityHitpoints);
            }

            var attackAttribute = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttribute != null) {
                attackAttribute.setBaseValue(entityData.entityAttackDamage);
            }
        }

        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired(); // Prevent despawning
        }

        level.addFreshEntity(entity);

        // Reset portal modifiers after boss spawn
        if (step == 5) {
            portalData.setModifiersRemoved(0);
        }
    }

    private BlockPos findSpawnPosition(ServerLevel level, PortalData portalData) {
        List<Block> blocksToSearch = List.of(
                Registry.BLOCK.get(new ResourceLocation("the_vault:chiseled_vault_stone")),
                Registry.BLOCK.get(new ResourceLocation("the_vault:chromatic_iron_block")),
                Registry.BLOCK.get(new ResourceLocation("the_vault:bumbo_polished_vault_stone")),
                Registry.BLOCK.get(new ResourceLocation("the_vault:vault_stone")),
                Registry.BLOCK.get(new ResourceLocation("the_vault:vault_cobblestone"))
        );

        BlockPos center = portalData.getPortalFrameCenterPos().below(2);
        BlockPos spawnPosition = isBlockNearby(level, center, blocksToSearch, 7);

        if(spawnPosition != null) {
            return spawnPosition;
        } else {
            OverVaults.LOGGER.warn("Could not find a proper spawn position, using portal center position");
            return portalData.getPortalFrameCenterPos();
        }
    }

    private EntityDataEntry getEntityForStep(int step) {
        switch(step) {
            case 1 -> {
                return TIER_1_ENTITY;
            }
            case 2 -> {
                return TIER_2_ENTITY;
            }
            case 3 -> {
                return TIER_3_ENTITY;
            }
            case 4 -> {
                return TIER_4_ENTITY;
            }
            default -> {
                return TIER_0_ENTITY;
            }
        }
    }

    public static BlockPos isBlockNearby(ServerLevel world, BlockPos center, List<Block> blocksToFind, int maxRadius) {
        List<BlockPos> validPositions = new ArrayList<>();
        for (int radius = 0; radius <= maxRadius; radius++) {

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (Math.abs(x) == radius || Math.abs(y) == radius || Math.abs(z) == radius) {
                            BlockPos pos = center.offset(x, y, z);
                            Block blockAtPos = world.getBlockState(pos).getBlock();

                            if (blocksToFind.contains(blockAtPos)) {
                                boolean isTransparent = world.getBlockState(pos.above()).getMaterial().isReplaceable()
                                        && world.getBlockState(pos.above(2)).getMaterial().isReplaceable();

                                if (isTransparent) {
                                    validPositions.add(pos);
                                }
                            }
                        }
                    }
                }
            }
        }

        return validPositions.isEmpty() ? null : validPositions.get(Config.rand.nextInt(validPositions.size()));
    }
}
