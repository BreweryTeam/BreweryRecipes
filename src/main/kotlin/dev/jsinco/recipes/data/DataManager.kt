package dev.jsinco.recipes.data

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.data.storage.StorageImpl
import dev.jsinco.recipes.data.storage.mysql.MySQLStorageImpl
import dev.jsinco.recipes.data.storage.sqlite.SQLiteStorageImpl
import java.io.File

class DataManager(val dataFolder: File) {

    val storageImpl: StorageImpl

    init {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        storageImpl = createStorage(StorageType.fromString(BreweryRecipes.recipesConfig.storage.type))
        storageImpl.createTables()
    }

    private fun createStorage(type: StorageType): StorageImpl {
        return when (type) {
            StorageType.SQLite -> SQLiteStorageImpl(dataFolder)
            StorageType.MySQL -> MySQLStorageImpl()
        }
    }

}