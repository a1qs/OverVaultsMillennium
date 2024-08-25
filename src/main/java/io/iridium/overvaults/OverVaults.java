package io.iridium.overvaults;

import com.mojang.logging.LogUtils;

import io.iridium.overvaults.world.structure.ModStructures;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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


        public static String vaultStoneBlock = "the_vault:polished_vault_stone";
        public static String vaultFighterEntity = "the_vault:vault_fighter_5";


        @SubscribeEvent
        public static void onAdvancement(AdvancementEvent event) {
            if (event.getAdvancement().getId().equals(new ResourceLocation("overvaults:exploration/root"))) {
                ServerLevel serverWorld = (ServerLevel) event.getPlayer().level;
                BlockPos pos = event.getPlayer().blockPosition();
                event.getPlayer().sendMessage(new TextComponent("send kelp").withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.RED), event.getPlayer().getUUID());
                event.getPlayer().sendMessage(new TextComponent("You think you can succeed? Where even I have failed?").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY), event.getPlayer().getUUID());


                //check for nearest polished vault stone
                BlockPos vaultStoneLoc = isBlockNearby(serverWorld, pos, Registry.BLOCK.get(new ResourceLocation(vaultStoneBlock)), 15);


                //spawn a vault fighter at the nearest polished vault stone
                if (vaultStoneLoc != null) {
                    EntityType<?> entityType = EntityType.byString(vaultFighterEntity).orElse(null);
                    if (entityType != null) {
                        Entity entity = entityType.create(serverWorld);
                        if (entity != null) {

                            serverWorld.playSound(null, vaultStoneLoc, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.MASTER, 2.0F, 1.0F);
                            serverWorld.playSound(null, vaultStoneLoc, SoundEvents.END_PORTAL_SPAWN, SoundSource.MASTER, 2.0F, 1.9F);

//                            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("the_vault:artifact_boss_death"));
//                            serverWorld.playSound(null, vaultStoneLoc, sound, SoundSource.MASTER, 2.0F, 1.0F);


                            entity.moveTo(vaultStoneLoc.getX() + 0.5, vaultStoneLoc.getY() + 1, vaultStoneLoc.getZ() + 0.5);
                            serverWorld.addFreshEntity(entity);


                        }
                    }
                }


            }
        }

        public static BlockPos isBlockNearby(ServerLevel world, BlockPos center, Block blockToFind, int maxRadius) {
            for (int radius = 0; radius <= maxRadius; radius++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (Math.abs(x) == radius || Math.abs(y) == radius || Math.abs(z) == radius) {
                                BlockPos pos = center.offset(x, y, z);
                                if (world.getBlockState(pos).getBlock() == blockToFind) {

                                    // Check if blocks above are safe to spawn
                                    boolean isAir = world.getBlockState(pos.above()).isAir() && world.getBlockState(pos.above(2)).isAir();
                                    boolean isWater = world.getBlockState(pos.above()).getFluidState().isSource() && world.getBlockState(pos.above(2)).getFluidState().isSource();
                                    boolean isSnow = world.getBlockState(pos.above()).getBlock() == Blocks.SNOW;

                                    if (isAir || isWater || isSnow) {
                                        return pos;
                                    }
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


