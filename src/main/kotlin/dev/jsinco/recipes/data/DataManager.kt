package dev.jsinco.recipes.data

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.data.storage.MySQLStorageImpl
import dev.jsinco.recipes.data.storage.SQLiteStorageImpl
import dev.jsinco.recipes.data.storage.YAMLStorageImpl
import java.io.File
import java.util.*

class DataManager(private val dataFolder: File) {

    init {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        getSelectedStorage()
    }

    companion object {
        private val loadedStorages: EnumMap<StorageType, StorageImpl> = EnumMap(StorageType::class.java)
    }

    @Synchronized
    private fun createStorage(type: StorageType): StorageImpl = when (type) {
        StorageType.SQLite -> SQLiteStorageImpl(dataFolder)
        StorageType.MySQL -> MySQLStorageImpl(dataFolder)
        StorageType.YAML -> YAMLStorageImpl(dataFolder)
    }

    @Synchronized
    fun getStorage(type: StorageType): StorageImpl = loadedStorages.computeIfAbsent(type) { createStorage(it) }

    fun getSelectedStorage(): StorageImpl = getStorage(StorageType.fromString(Recipes.recipesConfig.storage.type))

    fun getDataFolder(): File = dataFolder

}