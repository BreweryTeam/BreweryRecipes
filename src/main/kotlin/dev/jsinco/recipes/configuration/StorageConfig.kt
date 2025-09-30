package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

class StorageConfig : OkaeriConfig() {

    @Comment(
        "Pick the storage method that suits you best:",
        " - SQLite -> Lightweight, best for single-server use (default)",
        " - MySQL -> Ideal for syncing across multiple servers",
        " - YAML -> Human-readable, great for debugging",
        "Leave at SQLite if unsure"
    )
    var type: String? = "SQLite"

    @Comment("MySQL-specific configuration settings")
    var mysql: MySQLOptions = MySQLOptions()

    class MySQLOptions : OkaeriConfig() {
        @Comment("The IP address or hostname of your MySQL server")
        var host: String = "localhost"

        @Comment("The port number MySQL is listening on (default is 3306)")
        var port: Int = 3306

        @Comment("The username for connecting to the MySQL database")
        var user: String = "recipes"

        @Comment("The password for the specified MySQL user")
        var password: String = "supersafe"

        @Comment("Name of the database to store bodyhealth data in")
        var database: String = "recipes"

        @Comment(
            "A prefix for table names to avoid conflicts",
            "with other plugins when sharing a database"
        )
        var prefix: String = "brew_"
    }

}