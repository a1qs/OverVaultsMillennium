package io.iridium.overvaults.millenium.util;

import com.google.gson.*;
import iskallia.vault.core.Version;
import iskallia.vault.core.vault.VaultRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class JsonUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static JsonObject readJsonFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeJsonFile(File file, JsonObject jsonObject) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(jsonObject, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JsonObject getVaultLootTable(ResourceLocation lootTablelocation) {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path fullPath = gameDir.resolve(VaultRegistry.LOOT_TABLE.getKey(lootTablelocation).get(Version.latest()).getPath());

        return JsonUtils.readJsonFile(fullPath.toFile());
    }
}
