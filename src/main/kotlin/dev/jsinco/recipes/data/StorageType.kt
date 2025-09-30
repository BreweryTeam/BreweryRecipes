package dev.jsinco.recipes.data

enum class StorageType {

    YAML, MySQL, SQLite;

    companion object {
        @JvmStatic
        fun fromString(type: String?): StorageType {
            if (type == null) return SQLite
            return when (type.trim().lowercase()) {
                "mysql" -> MySQL
                "yaml" -> YAML
                else -> SQLite
            }
        }
    }
}