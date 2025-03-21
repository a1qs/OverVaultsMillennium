package io.iridium.overvaults.millenium.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.iridium.overvaults.millenium.util.JsonUtil;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.LootTableKey;
import iskallia.vault.core.vault.VaultRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LootTableCommands extends BaseCommand {
    @Override
    public String getName() {
        return "lootTables";
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("removeItemFromLootTable")
                .then(Commands.argument("lootTable", ResourceLocationArgument.id())
                        .suggests(this::suggestLootTables)
                        .then(Commands.argument("itemId", StringArgumentType.string())
                                .executes(this::removeItemId)
                        )
                )
        );
        builder.then(Commands.literal("addLootTableEntry")
                .then(Commands.argument("lootTable", ResourceLocationArgument.id())
                        .suggests(this::suggestLootTables)
                        .then(Commands.argument("weight", StringArgumentType.string())
                                .suggests(new LootTableCommands.WeightSuggestionProvider())
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
        );
    }

    private int addLootTableValue(CommandContext<CommandSourceStack> context) {
        ResourceLocation lootTablelocation = ResourceLocationArgument.getId(context, "lootTable");
        int weight = Integer.parseInt(StringArgumentType.getString(context, "weight"));

        String itemId = StringArgumentType.getString(context, "itemId");
        int newWeight = IntegerArgumentType.getInteger(context, "itemWeight");
        int minCount = IntegerArgumentType.getInteger(context, "minCount");
        int maxCount = IntegerArgumentType.getInteger(context, "maxCount");

        Path gameDir = FMLPaths.GAMEDIR.get();
        Path fullPath = gameDir.resolve(VaultRegistry.LOOT_TABLE.getKey(lootTablelocation).get(Version.latest()).getPath());



        JsonObject obj = JsonUtil.readJsonFile(fullPath.toFile());
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
                    JsonUtil.writeJsonFile(fullPath.toFile(), obj);
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
        JsonObject obj = JsonUtil.readJsonFile(fullPath.toFile());

        if(obj != null) {
            if(!removeItemById(obj, itemId)) {
                context.getSource().sendFailure(new TextComponent("Item \"" + itemId + "\" is not in Loottable \"" + lootTablelocation + "\"!"));
                return 1;
            }

            JsonUtil.writeJsonFile(fullPath.toFile(), obj);
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




    // Util methods.
    private static boolean removeItemById(JsonObject jsonObject, String itemId) {
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
            JsonObject obj = JsonUtil.getVaultLootTableJsonObject(resourceLocation);
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
