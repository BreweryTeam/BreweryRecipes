package dev.jsinco.recipes.data.storage

import dev.jsinco.recipes.data.StorageImpl
import dev.jsinco.recipes.data.StorageType
import java.io.File

class YAMLStorageImpl(private val dataFolder: File) : StorageImpl {

    override fun getType(): StorageType = StorageType.YAML

    private val file: File = File(dataFolder, "data.yaml")

    init {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        if (!file.exists()) file.createNewFile()
    }

}