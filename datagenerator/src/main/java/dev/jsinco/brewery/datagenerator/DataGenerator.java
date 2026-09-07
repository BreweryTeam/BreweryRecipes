package dev.jsinco.brewery.datagenerator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DataGenerator {

    private static final Map<String, Integer> BIOME_COLORED_BLOCKS = biomeColoredBlocks();
    private static Map<String, Integer> biomeColoredBlocks() {
        Map<String, Integer> blocks = new LinkedHashMap<>();
        blocks.put("water_still", 0x3f76e4);
        blocks.put("water_flow", 0x3f76e4);
        blocks.put("redstone_dust", 0xfc3100);
        blocks.put("melon_stem", 0x8ab348);
        blocks.put("pumpkin_stem", 0x8ab348);
        blocks.put("grass_block_top", 0x7cbd6b);
        blocks.put("grass_block_side_overlay", 0x7cbd6b);
        blocks.put("short_grass", 0x7cbd6b);
        blocks.put("tall_grass", 0x7cbd6b);
        blocks.put("fern", 0x7cbd6b);
        blocks.put("_leaves", 0x71a74d);
        blocks.put("vine", 0x48b518);
        blocks.put("sugar_cane", 0x8eb971);
        blocks.put("lily_pad", 0x208030);
        blocks.put("seagrass", 0x4d9e3f);
        blocks.put("kelp", 0x4d9e3f);
        blocks.put("dry_grass", 0xa89060);
        blocks.put("dry_bush", 0x946b44);
        blocks.put("bush", 0x71a74d);
        return Collections.unmodifiableMap(blocks);
    }

    private static final String PISTON_META_GAME_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final Pattern LANG_RE = Pattern.compile("minecraft/lang/([^.]+)\\.json");

    public static void main(String[] args) throws URISyntaxException, IOException {
        if (args.length != 1) {
            System.out.print("Usage: <target folder>");
            System.exit(1);
        }
        File outputFolder = new File(args[0]);
        URL url1 = ClassLoader.getSystemResource("assets/minecraft/textures/item/apple.png");
        URL url2 = ClassLoader.getSystemResource("assets/minecraft/textures/block/acacia_log.png");
        URI uri1 = url1.toURI();
        URI uri2 = url2.toURI();
        String version = System.getProperty("minecraft.version");
        if (version.isBlank()) {
            throw new IllegalArgumentException("Invalid version");
        }
        try {
            generateColorData(List.of(Paths.get(uri1), Paths.get(uri2)), outputFolder);
        } catch (FileSystemNotFoundException e) {
            try (FileSystem ignored = FileSystems.newFileSystem(uri1, Collections.emptyMap())) {
                generateColorData(List.of(Paths.get(uri1), Paths.get(uri2)), outputFolder);
            }
        }
        generateClientSideTranslations(outputFolder, version);
    }

    private static void generateClientSideTranslations(File outputFolder, String version) throws IOException {
        File versionFile = new File(outputFolder, "locale.version.json");
        if (versionFile.isFile()) {
            try (InputStream inputStream = new FileInputStream(versionFile); InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                if (JsonParser.parseReader(inputStreamReader) instanceof JsonObject jsonObject
                        && jsonObject.get("version") instanceof JsonPrimitive jsonPrimitive
                        && jsonPrimitive.getAsString().equals(version)
                ) {
                    return;
                }
            } catch (JsonParseException ignored) {
            }
        }
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(PISTON_META_GAME_MANIFEST))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        String versionUri = null;
        try (HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException(String.format("HTTP response %s: %s", response.statusCode(), response.body()));
            }
            JsonElement versionManifest = JsonParser.parseString(response.body());
            if (!(versionManifest instanceof JsonObject jsonObject)) {
                throw new IllegalArgumentException("Invalid format of game manifest!");
            }
            for (JsonElement versionData : jsonObject.get("versions").getAsJsonArray()) {
                if (versionData instanceof JsonObject jsonObject1 && Objects.equals(jsonObject1.get("id").getAsString(), version)) {
                    versionUri = jsonObject1.get("url").getAsString();
                    break;
                }
            }
            if (versionUri == null) {
                throw new IllegalArgumentException("Version not found");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        HttpRequest httpRequest2 = HttpRequest.newBuilder()
                .uri(URI.create(versionUri))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        String assetsUri;
        try (HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            HttpResponse<String> response = httpClient.send(httpRequest2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException(String.format("HTTP response %s: %s", response.statusCode(), response.body()));
            }
            JsonElement versionMeta = JsonParser.parseString(response.body());
            if (!(versionMeta instanceof JsonObject jsonObject)) {
                throw new IllegalArgumentException("Invalid format of game manifest!");
            }
            assetsUri = jsonObject.get("assetIndex").getAsJsonObject().get("url").getAsString();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        HttpRequest httpRequest3 = HttpRequest.newBuilder()
                .uri(URI.create(assetsUri))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try (HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            HttpResponse<String> response = httpClient.send(httpRequest3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException(String.format("HTTP response %s: %s", response.statusCode(), response.body()));
            }
            JsonElement versionMeta = JsonParser.parseString(response.body());
            if (!(versionMeta instanceof JsonObject jsonObject)) {
                throw new IllegalArgumentException("Invalid format of game manifest!");
            }
            for (Map.Entry<String, JsonElement> entry : jsonObject.get("objects").getAsJsonObject().entrySet()) {
                Matcher matcher = LANG_RE.matcher(entry.getKey());
                if (!matcher.matches()) {
                    continue;
                }
                Locale locale = Locale.forLanguageTag(matcher.group(1).replace("_", "-"));
                downloadLangFile(locale, new File(outputFolder, "lang"), entry.getValue().getAsJsonObject().get("hash").getAsString());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JsonObject versionFileOutput = new JsonObject();
        versionFileOutput.addProperty("version", version);
        JsonUtil.dump(versionFileOutput, versionFile);
    }

    private static void downloadLangFile(Locale locale, File targetFolder, String hash) throws IOException {
        File destinationFile = new File(targetFolder, "%s.json".formatted(locale.toLanguageTag()));
        String urlString = "https://resources.download.minecraft.net/%s/%s".formatted(hash.substring(0, 2), hash);
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try (InputStream inputStream = connection.getInputStream(); InputStreamReader reader = new InputStreamReader(inputStream)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, JsonElement> property : jsonObject.entrySet()) {
                if (!property.getKey().startsWith("item.minecraft.") && !property.getKey().startsWith("block.minecraft.")) {
                    toRemove.add(property.getKey());
                }
            }
            toRemove.forEach(jsonObject::remove);
            JsonUtil.dump(jsonObject, destinationFile);
        }
    }

    private static void generateColorData(List<Path> paths, File outputFolder) throws IOException {
        JsonObject jsonObject = new JsonObject();
        for (Path path : paths) {
            Path directory = path.getParent();
            try (Stream<Path> walk = Files.walk(directory)) {
                Iterator<Path> walkIterator = walk.iterator();
                while (walkIterator.hasNext()) {
                    Path next = walkIterator.next();
                    if (!next.toString().endsWith(".png")) {
                        continue;
                    }
                    String name = next.getFileName().toString().replace(".png", "");
                    if (jsonObject.has(name)) {
                        continue;
                    }
                    try (InputStream inputStream = Files.newInputStream(next)) {
                        BufferedImage image = ImageIO.read(inputStream);
                        Color color = replaceBiomeColors(ColorUtil.getDistinctColor(image), name);
                        jsonObject.addProperty(name, Integer.toHexString(color.getRGB() & 0x00ffffff));
                    }
                }
            }
        }
        addRegistryColors(paths.get(0), jsonObject);
        JsonUtil.dump(jsonObject, new File(outputFolder, "item-colors.json"));
    }

    private static void addRegistryColors(Path texturePath, JsonObject jsonObject) throws IOException {
        Path assetsRoot = texturePath.getParent().getParent().getParent();
        ModelResolver modelResolver = new ModelResolver(assetsRoot);
        Map<String, String> colors = new HashMap<>();
        for (String registryName : modelResolver.registryNames()) {
            String texture = modelResolver.resolve(registryName);
            if (texture == null) continue;
            String color = colors.get(texture);
            if (color == null) {
                color = readColor(assetsRoot.resolve("textures/" + texture + ".png"), ModelResolver.fileName(texture));
                if (color == null) continue;
                colors.put(texture, color);
            }
            jsonObject.addProperty(registryName, color);
        }
    }

    private static @Nullable String readColor(Path texture, String textureName) throws IOException {
        if (!Files.isRegularFile(texture)) return null;
        try (InputStream inputStream = Files.newInputStream(texture)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) return null;
            Color color = replaceBiomeColors(ColorUtil.getDistinctColor(image), textureName);
            return Integer.toHexString(color.getRGB() & 0x00ffffff);
        }
    }

    private static Color replaceBiomeColors(Color initial, String name) {
        for (String edgeCase : BIOME_COLORED_BLOCKS.keySet()) {
            if (name.contains(edgeCase)) {
                return new Color(BIOME_COLORED_BLOCKS.get(edgeCase));
            }
        }
        return initial;
    }
}
