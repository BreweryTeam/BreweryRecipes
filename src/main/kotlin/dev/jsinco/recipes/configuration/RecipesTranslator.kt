package dev.jsinco.recipes.configuration

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator
import java.io.*
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class RecipesTranslator(private val localeDirectory: File) : MiniMessageTranslator() {

    private var translations: Map<Locale, Properties>

    init {
        syncLangFiles()
        translations = loadLangFiles()
    }

    fun reload() {
        syncLangFiles()
        translations = loadLangFiles()
    }

    private fun syncLangFiles() {
        check(!(!localeDirectory.exists() && !localeDirectory.mkdirs())) { "Failed to create locale directory at " + localeDirectory.absolutePath }

        try {
            val resources = javaClass.getClassLoader().getResources("locale")
            while (resources.hasMoreElements()) {
                val url = resources.nextElement()

                (if ("jar" == url.protocol) FileSystems.newFileSystem(
                    url.toURI(),
                    mutableMapOf<String?, Any?>()
                ) else null).use { fs ->
                    val internalLocaleDir = Paths.get(url.toURI())
                    Files.newDirectoryStream(internalLocaleDir, "*.properties").use { stream ->
                        for (path in stream) {
                            mergeAndStoreProperties(path)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to sync language files", e)
        } catch (e: URISyntaxException) {
            throw RuntimeException("Failed to sync language files", e)
        }
        // special thanks to StackOverflow and other useful sites lol
    }

    @Throws(IOException::class)
    private fun mergeAndStoreProperties(internalFile: Path) {
        val fileName = internalFile.fileName.toString()
        val externalFile = File(localeDirectory, fileName)

        val internalProps = Properties()
        Files.newBufferedReader(internalFile, StandardCharsets.UTF_8).use { reader ->
            internalProps.load(reader)
        }
        val merged = Properties()
        if (externalFile.exists()) {
            val externalProps = Properties()
            InputStreamReader(FileInputStream(externalFile), StandardCharsets.UTF_8).use { reader ->
                externalProps.load(reader)
            }
            merged.putAll(externalProps)
            for (key in internalProps.stringPropertyNames()) {
                merged.putIfAbsent(key, internalProps.getProperty(key))
            }
        } else {
            merged.putAll(internalProps)
            if (!externalFile.createNewFile()) {
                throw IOException("Could not create file: $externalFile")
            }
        }
        OutputStreamWriter(FileOutputStream(externalFile), StandardCharsets.UTF_8).use { writer ->
            storeWithoutComments(merged, writer)
        }
    }

    @Throws(IOException::class)
    private fun storeWithoutComments(props: Properties, writer: Writer) {
        val keys: MutableList<String> = ArrayList<String>(props.stringPropertyNames())
        keys.sort()

        for (key in keys) {
            writer.write(key + "=" + props.getProperty(key) + "\n")
        }
    }

    private fun loadLangFiles() : Map<Locale, Properties> {
        require(localeDirectory.isDirectory()) { "Locale directory is not a directory!" }
        val translationsBuilder = ImmutableMap.Builder<Locale, Properties>()
        for (translationFile in localeDirectory.listFiles { file: File? ->
            file?.getName()?.endsWith(".properties") ?: false
        }) {
            try {
                FileInputStream(translationFile).use { inputStream ->
                    val translation = Properties()
                    translation.load(InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    val locale = Locale.forLanguageTag(translationFile.getName().replace(".properties$".toRegex(), ""))
                    if (locale != null) {
                        translationsBuilder.put(locale, translation)
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        val output = translationsBuilder.build()
        Preconditions.checkArgument(
            output.containsKey(Locale.ENGLISH),
            "Unknown translation: " + Locale.ENGLISH
        )
        return output
    }

    override fun name(): Key {
        return Key.key("recipes:global_translator")
    }

    public override fun getMiniMessageString(key: String, locale: Locale): String? {
        val translation: Properties? = this.translations[Locale.ENGLISH]
        Preconditions.checkState(translation != null, "Should have found a translation!")
        return translation!!.getProperty(key)
    }
}