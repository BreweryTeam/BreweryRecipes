package dev.jsinco.recipes.configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

//TODO make locale configurable /// placeholder

/**
 * Copy-pasted from TBP, can't bother to migrate to kotlin
 */
public class RecipesTranslator extends MiniMessageTranslator {

    private Map<Locale, Properties> translations;
    private final File localeDirectory;

    public RecipesTranslator(File localeDirectory) {
        this.localeDirectory = localeDirectory;
    }

    public void reload() {
        syncLangFiles();
        loadLangFiles();
    }

    private void syncLangFiles() {
        if (!localeDirectory.exists() && !localeDirectory.mkdirs()) { // shouldn't happen
            throw new IllegalStateException("Failed to create locale directory at " + localeDirectory.getAbsolutePath());
        }

        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("locale");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                try (FileSystem fs = "jar".equals(url.getProtocol()) ? FileSystems.newFileSystem(url.toURI(), Collections.emptyMap()) : null) {

                    Path internalLocaleDir = fs == null ? Paths.get(url.toURI()) : fs.getPath("locale");
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(internalLocaleDir, "*.properties")) {
                        for (Path path : stream) {
                            mergeAndStoreProperties(path);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to sync language files", e);
        }
        // special thanks to StackOverflow and other useful sites lol
    }

    private void mergeAndStoreProperties(Path internalFile) throws IOException {
        String fileName = internalFile.getFileName().toString();
        File externalFile = new File(localeDirectory, fileName);

        Properties internalProps = new Properties();
        try (Reader reader = Files.newBufferedReader(internalFile, StandardCharsets.UTF_8)) {
            internalProps.load(reader);
        }

        Properties merged = new Properties();
        if (externalFile.exists()) {

            Properties externalProps = new Properties();
            try (Reader reader = new InputStreamReader(new FileInputStream(externalFile), StandardCharsets.UTF_8)) {
                externalProps.load(reader);
            }

            merged.putAll(externalProps);
            for (String key : internalProps.stringPropertyNames()) {
                merged.putIfAbsent(key, internalProps.getProperty(key));
            }

        } else {
            merged.putAll(internalProps);
            if (!externalFile.createNewFile()) {
                throw new IOException("Could not create file: " + externalFile);
            }
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(externalFile), StandardCharsets.UTF_8)) {
            storeWithoutComments(merged, writer);
        }
    }

    private void storeWithoutComments(Properties props, Writer writer) throws IOException {
        List<String> keys = new ArrayList<>(props.stringPropertyNames());
        Collections.sort(keys);

        for (String key : keys) {
            writer.write(key + "=" + props.getProperty(key) + "\n");
        }
    }

    private void loadLangFiles() {
        if (!localeDirectory.isDirectory()) {
            throw new IllegalArgumentException("Locale directory is not a directory!");
        }
        ImmutableMap.Builder<Locale, Properties> translationsBuilder = new ImmutableMap.Builder<>();
        for (File translationFile : localeDirectory.listFiles(file -> file.getName().endsWith(".properties"))) {
            try (InputStream inputStream = new FileInputStream(translationFile)) {
                Properties translation = new Properties();
                translation.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                Locale locale = Locale.forLanguageTag(translationFile.getName().replaceAll(".properties$", ""));
                if (locale != null) {
                    translationsBuilder.put(locale, translation);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.translations = translationsBuilder.build();
        Preconditions.checkArgument(translations.containsKey(Locale.ENGLISH), "Unknown translation: " + Locale.ENGLISH);
    }

    @Override
    public @NotNull Key name() {
        return Key.key("recipes:global_translator");
    }

    @Override
    public @Nullable String getMiniMessageString(@NotNull String key, @NotNull Locale locale) {
        Properties translations = this.translations.get(Locale.ENGLISH);
        Preconditions.checkState(translations != null, "Should have found a translation!");
        return translations.getProperty(key);
    }
}
