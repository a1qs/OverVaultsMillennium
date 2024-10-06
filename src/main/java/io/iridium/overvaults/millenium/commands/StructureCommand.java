package io.iridium.overvaults.millenium.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.millenium.event.ServerTickEvent;
import io.iridium.overvaults.millenium.util.JsonUtils;
import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.LootTableKey;
import iskallia.vault.core.vault.VaultRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class StructureCommand {

    public StructureCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(OverVaults.MOD_ID)
                .requires(sender -> sender.hasPermission(4))
                .then(Commands.literal("getStructureWithIndex")
                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                .executes(this::getStructureWithIndex)
                        )
                )
                .then(Commands.literal("getRandomStructure")
                        .executes(this::getRandomStructure)
                )
                .then(Commands.literal("getNextOverVaultSpawn")
                        .executes(this::getNextOverVaultSpawn)
                )

                .then(Commands.literal("getActiveOverVault")
                        .executes(this::getActiveOverVault)
                )
                .then(Commands.literal("removeItemFromLootTable")
                        .then(Commands.argument("lootTable", ResourceLocationArgument.id())
                                .suggests(this::suggestLootTables)
                                .then(Commands.argument("itemId", StringArgumentType.string())
                                        .executes(this::removeItemId)
                                )
                        )
                )
                .then(Commands.literal("addLootTableEntry")
                        .then(Commands.argument("lootTable", ResourceLocationArgument.id())
                                .suggests(this::suggestLootTables)
                                .then(Commands.argument("weight", StringArgumentType.string())
                                        .suggests(new WeightSuggestionProvider())
                                        .then(Commands.argument("itemId", StringArgumentType.string())
                                                .then(Commands.argument("itemWeight", IntegerArgumentType.integer())
                                                        .then(Commands.argument("minCount", IntegerArgumentType.integer(1, 64))
                                                                .then(Commands.argument("maxCount", IntegerArgumentType.integer(1, 64))
                                                                        .executes(this::addLootTableValue)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private int addLootTableValue(CommandContext<CommandSourceStack> context) {
        ResourceLocation lootTablelocation = ResourceLocationArgument.getId(context, "lootTable");
        int weight = Integer.parseInt(StringArgumentType.getString(context, "weight"));
        System.out.println(weight);

        String itemId = StringArgumentType.getString(context, "itemId");
        int newWeight = IntegerArgumentType.getInteger(context, "itemWeight");
        int minCount = IntegerArgumentType.getInteger(context, "minCount");
        int maxCount = IntegerArgumentType.getInteger(context, "maxCount");

        Path gameDir = FMLPaths.GAMEDIR.get();
        Path fullPath = gameDir.resolve(VaultRegistry.LOOT_TABLE.getKey(lootTablelocation).get(Version.latest()).getPath());



        JsonObject obj = JsonUtils.readJsonFile(fullPath.toFile());
        if(obj == null) return 1;
        JsonArray entries = obj.getAsJsonArray("entries");
        for (JsonElement entryElement : entries) {
            JsonObject entry = entryElement.getAsJsonObject();
            JsonArray pools = entry.getAsJsonArray("pool");

            for (JsonElement poolElement : pools) {
                JsonObject pool = poolElement.getAsJsonObject();

                // Check if this pool has the weight you want to modify
                if (pool.has("weight") && pool.get("weight").getAsInt() == weight) {
                    // Get the pool's entry array (where items are stored)
                    JsonArray subPool = pool.getAsJsonArray("pool");

                    JsonObject newItemEntry = new JsonObject();
                    newItemEntry.addProperty("weight", newWeight);

                    JsonObject itemObject = new JsonObject();
                    itemObject.addProperty("id", itemId);

                    // Create the count object with min/max values
                    JsonObject countObject = new JsonObject();
                    countObject.addProperty("type", "uniform");
                    countObject.addProperty("min", minCount);
                    countObject.addProperty("max", maxCount);

                    // Add the count object to the item entry
                    itemObject.add("count", countObject);

                    // Add the item to the new entry
                    newItemEntry.add("item", itemObject);

                    // Add the new item entry to the pool
                    subPool.add(newItemEntry);
                    JsonUtils.writeJsonFile(fullPath.toFile(), obj);
                    String command = "/the_vault reloadcfg gen";
                    MutableComponent cmp0 = new TextComponent("Reload Gen Configs?").withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.BOLD);
                    MutableComponent cmp1 = new TextComponent("Loot tables have changed. ").withStyle(ChatFormatting.AQUA).append(cmp0);
                    cmp0.withStyle((style) ->
                            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to reload Configs!"))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
                    context.getSource().sendSuccess(cmp1, true);
                    return 0;
                }
            }
        }

        return 1;
    }

    private int removeItemId(CommandContext<CommandSourceStack> context) {
        ResourceLocation lootTablelocation = ResourceLocationArgument.getId(context, "lootTable");
        String itemId = StringArgumentType.getString(context, "itemId");

        Path gameDir = FMLPaths.GAMEDIR.get();
        if(VaultRegistry.LOOT_TABLE.getKey(lootTablelocation) == null) {
            context.getSource().sendFailure(new TextComponent("Invalid Loot table!"));
            return 1;
        }
        Path fullPath = gameDir.resolve(VaultRegistry.LOOT_TABLE.getKey(lootTablelocation).get(Version.latest()).getPath());
        JsonObject obj = JsonUtils.readJsonFile(fullPath.toFile());

        if(obj != null) {
            if(!removeItemById(obj, itemId)) {
                context.getSource().sendFailure(new TextComponent("Item \"" + itemId + "\" is not in Loottable \"" + lootTablelocation + "\"!"));
                return 1;
            }

            JsonUtils.writeJsonFile(fullPath.toFile(), obj);
            String command = "/the_vault reloadcfg gen";
            MutableComponent cmp0 = new TextComponent("Reload Gen Configs?").withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.BOLD);
            MutableComponent cmp1 = new TextComponent("Loot tables have changed. ").withStyle(ChatFormatting.AQUA).append(cmp0);
            cmp0.withStyle((style) ->
                    style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to reload Configs!"))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
            context.getSource().sendSuccess(cmp1, true);
            return 0;
        }
        context.getSource().sendFailure(new TextComponent("If this message occurs, please report to 'a1qs' on Discord"));
        return 1;
    }




    private int getStructureWithIndex(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        int index = IntegerArgumentType.getInteger(context, "index");

        List<ServerLevel> dimensions = Arrays.asList(
                server.getLevel(Level.OVERWORLD),
                server.getLevel(Level.NETHER),
                server.getLevel(Level.END)
        );

        List<ServerLevel> validDimensions = dimensions.stream()
                .filter(Objects::nonNull)
                .toList();

        List<PortalData> unifiedPortalDataList = new ArrayList<>();

        for (ServerLevel level : validDimensions) {
            List<PortalData> portalDataList = PortalSavedData.get(level).getPortalData();
            if(!portalDataList.isEmpty()) unifiedPortalDataList.addAll(portalDataList);
        }

        if (unifiedPortalDataList.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("No valid portal data found across all dimensions!"));
            return 1;
        }

        if (index >= unifiedPortalDataList.size()) {
            context.getSource().sendFailure(new TextComponent("Index: " + index + " is out of bounds! Max allowed value is: " + (unifiedPortalDataList.size() - 1)));
            return 1;
        }

        PortalData data = unifiedPortalDataList.get(index);
        MutableComponent cmp = new TextComponent("=== ")
                .append(new TextComponent("Portal data of index " + index).withStyle(ChatFormatting.AQUA))
                .append(new TextComponent(" ===\n").withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("    Portal Frame Center Position: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getPortalFrameCenterPos() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal size: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getSize() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Rotation: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getRotation() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Activation State: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getActiveState() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Dimension: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getDimension().location().getPath()).withStyle(ChatFormatting.YELLOW));

        context.getSource().sendSuccess(cmp, true);
        return 0;
    }

    private int getRandomStructure(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();

        List<PortalData> unifiedPortalDataList = PortalUtil.getAllLevelPortalData(server);
        if(unifiedPortalDataList == null) {
            context.getSource().sendFailure(new TextComponent("No valid portal data found across all dimensions!"));
            return 1;
        }

        int index = new Random().nextInt(unifiedPortalDataList.size());
        PortalData data = unifiedPortalDataList.get(index);
        MutableComponent cmp = new TextComponent("=== ")
                .append(new TextComponent("Portal data of index " + index).withStyle(ChatFormatting.AQUA))
                .append(new TextComponent(" ===\n").withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("    Portal Frame Center Position: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getPortalFrameCenterPos() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal size: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getSize() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Rotation: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getRotation() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Activation State: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getActiveState() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Dimension: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getDimension().location().getPath()).withStyle(ChatFormatting.YELLOW));

        context.getSource().sendSuccess(cmp, true);
        return 0;
    }

    private int getNextOverVaultSpawn(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if(PortalUtil.getAllLevelActivePortalData(ServerLifecycleHooks.getCurrentServer()) != null) {
            source.sendFailure(new TextComponent("An OverVaults portal is already active! Cannot spawn additional."));
            return 1;
        }

        int ticksRemaining = ServerTickEvent.ticksForPortalSpawn - ServerTickEvent.counter;
        int totalSecondsRemaining  = ticksRemaining/20;

        int hours = totalSecondsRemaining / 3600;
        int minutes = (totalSecondsRemaining % 3600) / 60;
        int seconds = totalSecondsRemaining % 60;
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        MutableComponent cmp = new TextComponent("")
                .append(new TextComponent("Time until next OverVault portal spawn: ").withStyle(ChatFormatting.GREEN))
                .append(new TextComponent(timeFormatted).withStyle(ChatFormatting.AQUA))
                .append(new TextComponent(" (HH:MM:SS)").withStyle(ChatFormatting.GRAY));



        source.sendSuccess(cmp, true);

        return 0;
    }

    private int getActiveOverVault(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();

        List<PortalData> unifiedPortalDataList = PortalUtil.getAllLevelPortalData(server);
        if(unifiedPortalDataList == null) {
            context.getSource().sendFailure(new TextComponent("No valid portal data found across all dimensions!"));
            return 1;
        }

        PortalData data = unifiedPortalDataList.stream()
                .filter(PortalData::getActiveState)
                .findFirst()
                .orElse(null);

        if(data == null) {
            context.getSource().sendFailure(new TextComponent("No active OverVault found!"));
            return 1;
        }

        BlockPos offsetPosition =  data.getPortalFrameCenterPos().offset(3.0, 0.0, 3.0);
        String tpCommand = "    /execute as @s in " + data.getDimension().location() + " run tp " + offsetPosition.getX() + " " + offsetPosition.getY() + " " + offsetPosition.getZ();
        MutableComponent tpComponent = new TextComponent(tpCommand).withStyle(ChatFormatting.AQUA);
        tpComponent.withStyle((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to teleport!"))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpCommand)));

        MutableComponent cmp = new TextComponent("=== ")
                .append(new TextComponent("Portal data of current active OverVault").withStyle(ChatFormatting.AQUA))
                .append(new TextComponent(" ===\n").withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("    Portal Frame Center Position: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getPortalFrameCenterPos() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal size: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getSize() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Rotation: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getRotation() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Activation State: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getActiveState() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Dimension: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getDimension().location().getPath() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(tpComponent);

        context.getSource().sendSuccess(cmp, true);
        return 0;
    }



    public static boolean removeItemById(JsonObject jsonObject, String itemId) {
        JsonArray entries = jsonObject.getAsJsonArray("entries");
        boolean itemRemoved = false;

        for (JsonElement entryElement : entries) {
            JsonObject entry = entryElement.getAsJsonObject();
            JsonArray pools = entry.getAsJsonArray("pool");

            for (JsonElement poolElement : pools) {
                JsonObject pool = poolElement.getAsJsonObject();
                JsonArray subPool = pool.getAsJsonArray("pool");

                for (int i = 0; i < subPool.size(); i++) {
                    JsonObject itemObject = subPool.get(i).getAsJsonObject();
                    JsonObject item = itemObject.getAsJsonObject("item");

                    if (item.get("id").getAsString().equals(itemId)) {
                        subPool.remove(i);
                        itemRemoved = true;
                        break;
                    }
                }
            }
        }
        return itemRemoved;
    }


    private CompletableFuture<Suggestions> suggestLootTables(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<LootTableKey> keys = VaultRegistry.LOOT_TABLE.getKeys();

        for (LootTableKey key : keys) {
            if (key.getId().toString().startsWith(builder.getRemaining())) {
                builder.suggest(key.getId().toString());
            }
        }

        return builder.buildFuture();
    }


    static class WeightSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
            ResourceLocation resourceLocation = ResourceLocationArgument.getId(context, "lootTable");
            List<String> availableWeights = getWeightsForResourceLocation(resourceLocation);
            availableWeights.forEach(builder::suggest);
            return builder.buildFuture();
        }

        private List<String> getWeightsForResourceLocation(ResourceLocation resourceLocation) {
            JsonObject obj = JsonUtils.getVaultLootTable(resourceLocation);
            JsonArray entries = obj.getAsJsonArray("entries");
            List<String> weightList = new ArrayList<>();
            for (JsonElement entryElement : entries) {
                JsonObject entry = entryElement.getAsJsonObject();
                JsonArray pools = entry.getAsJsonArray("pool");

                for(JsonElement poolElement : pools) {
                    int weight = poolElement.getAsJsonObject().get("weight").getAsInt();
                    weightList.add(String.valueOf(weight));
                }
            }

            return weightList;
        }
    }



}
