package dev.jsinco.brewery.datagenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class JsonUtil {

    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void dump(JsonElement json, File destinationFile) throws IOException {
        File parentFile = destinationFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException("Could not create directory: " + parentFile);
        }
        if (!destinationFile.exists() && !destinationFile.createNewFile()) {
            throw new IOException("Could not create file: " + destinationFile);
        }
        try (PrintWriter writer = new PrintWriter(destinationFile, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("\t");
            gson.toJson(json, jsonWriter);
            writer.print("\n");
        }
    }
}
