package io.iridium.overvaults;

import com.mojang.logging.LogUtils;

import io.iridium.overvaults.world.structure.ModStructures;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Mod(OverVaults.MOD_ID)
public class OverVaults {

    public static final String MOD_ID = "overvaults";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OverVaults() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        ModStructures.register(modEventBus);

    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @Mod.EventBusSubscriber(modid = OverVaults.MOD_ID)
    public static class OverVaultsEventHandler {


        public static String vaultFighterEntity = "the_vault:vault_fighter_5";
//        public static String vaultFighterEntity = "minecraft:zombie";


        @SubscribeEvent
        public static void onAdvancement(AdvancementEvent event) {
            if (event.getAdvancement().getId().equals(new ResourceLocation("overvaults:exploration/root"))) {
                ServerLevel serverWorld = (ServerLevel) event.getPlayer().level;
                BlockPos pos = event.getPlayer().blockPosition();

                List<Block> blocksToSearch = new ArrayList<>();
                blocksToSearch.add(Registry.BLOCK.get(new ResourceLocation("the_vault:chiseled_vault_stone")));
                blocksToSearch.add(Registry.BLOCK.get(new ResourceLocation("the_vault:chromatic_iron_block")));
                blocksToSearch.add(Registry.BLOCK.get(new ResourceLocation("the_vault:bumbo_polished_vault_stone")));
//                blocksToSearch.add(Registry.BLOCK.get(new ResourceLocation("minecraft:deepslate_brick_wall")));
//                blocksToSearch.add(Registry.BLOCK.get(new ResourceLocation("minecraft:polished_blackstone_bricks")));
//                blocksToSearch.add(Registry.BLOCK.get(new ResourceLocation("minecraft:deepslate_bricks")));
//                blocksToSearch.add(Registry.BLOCK.get(new ResourceLocation("minecraft:deepslate_brick_slab")));

                //check for nearest polished vault stone
                BlockPos hunterSpawnPos = isBlockNearby(serverWorld, pos, blocksToSearch, 20);

                //spawn a vault fighter at the nearest polished vault stone
                if (hunterSpawnPos == null) {
                    removeAdvancement((ServerPlayer) event.getPlayer(), "overvaults:exploration/root");
                    return;
                }
                EntityType<?> entityType = EntityType.byString(vaultFighterEntity).orElse(null);
                if (entityType == null) {
                    removeAdvancement((ServerPlayer) event.getPlayer(), "overvaults:exploration/root");
                    return;
                }
                Entity entity = entityType.create(serverWorld);
                if (entity == null) {
                    removeAdvancement((ServerPlayer) event.getPlayer(), "overvaults:exploration/root");
                    return;
                }

                serverWorld.playSound(null, hunterSpawnPos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.MASTER, 1.0F, 1.0F);
                serverWorld.playSound(null, hunterSpawnPos, SoundEvents.END_PORTAL_SPAWN, SoundSource.MASTER, 1.0F, 0.2F);

                event.getPlayer().sendMessage(new TextComponent("please send kelp").withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.RED), event.getPlayer().getUUID());
                event.getPlayer().sendMessage(new TextComponent("You think you can succeed? Where even I have failed?").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY), event.getPlayer().getUUID());
                event.getPlayer().sendMessage(new TextComponent("please send kelp").withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.RED), event.getPlayer().getUUID());


                serverWorld.sendParticles(ParticleTypes.PORTAL, hunterSpawnPos.getX() + 0.5, hunterSpawnPos.getY() + 1, hunterSpawnPos.getZ() + 0.5, 8000, 1, 2.0, 1, 2);

                // Schedule the second sendParticles call with a delay

                MinecraftServer server = serverWorld.getServer();

                // Schedule the second sendParticles call with a delay
                ScheduledExecutorService scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);
                scheduler.schedule(() -> {
                    server.execute(() -> {
                        serverWorld.sendParticles(ParticleTypes.REVERSE_PORTAL, hunterSpawnPos.getX() + 0.5, hunterSpawnPos.getY() + 1, hunterSpawnPos.getZ() + 0.5, 6000, 0.5, 2.0, 0.5, 2);

                        entity.moveTo(hunterSpawnPos.getX() + 0.5, hunterSpawnPos.getY() + 1, hunterSpawnPos.getZ() + 0.5);
                        serverWorld.addFreshEntity(entity);
                    });
                }, 2300, TimeUnit.MILLISECONDS);


            }
        }

        public static void removeAdvancement(ServerPlayer player, String advancementID) {
            ResourceLocation advancementLoc = new ResourceLocation(advancementID);
            Advancement adv = player.getServer().getAdvancements().getAdvancement(advancementLoc);

            if (adv != null) {
                player.getAdvancements().revoke(adv, "trigger");
            }

        }


        public static BlockPos isBlockNearby(ServerLevel world, BlockPos center, List<Block> blocksToFind, int maxRadius) {
            for (int radius = 0; radius <= maxRadius; radius++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (Math.abs(x) == radius || Math.abs(y) == radius || Math.abs(z) == radius) {
                                BlockPos pos = center.offset(x, y, z);
                                if (blocksToFind.contains(world.getBlockState(pos).getBlock()) ) {
                                    // Check if blocks above are safe to spawn
                                    boolean isTransparent = world.getBlockState(pos.above()).getMaterial().isReplaceable() && world.getBlockState(pos.above(2)).getMaterial().isReplaceable();
                                    if (isTransparent) return pos;
                                }
                            }
                        }
                    }
                }
            }
            ;
            return null;
        }


    }
}


