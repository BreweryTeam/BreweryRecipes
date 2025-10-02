package dev.jsinco.recipes.data

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.data.storage.MySQLStorageImpl
import dev.jsinco.recipes.data.storage.SQLiteStorageImpl
import java.io.File

class DataManager(val dataFolder: File) {

    val storageImpl: StorageImpl

    init {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        storageImpl = createStorage(StorageType.fromString(Recipes.recipesConfig.storage.type))
        storageImpl.createTables()
    }

    private fun createStorage(type: StorageType): StorageImpl {
        return when (type) {
            StorageType.SQLite -> SQLiteStorageImpl(dataFolder)
            StorageType.MySQL -> MySQLStorageImpl()
        }
    }

}