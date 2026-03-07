package dev.jsinco.recipes.data.storage

import dev.jsinco.recipes.util.Logger
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class StorageSessionExecutor(private val executor: Executor, private val connectionSupplier: Function0<Connection>) {

    fun <T> runStatement(statement: String, supplier: Function1<PreparedStatement, T>): CompletableFuture<T?> {
        return CompletableFuture.supplyAsync<T>({
            connectionSupplier.invoke().use {
                it.prepareStatement(statement).use { preparedStatement ->
                    return@supplyAsync supplier.invoke(preparedStatement)
                }
            }
        }, executor)
            .handleAsync { t, e ->
                if (e != null) {
                    Logger.logErr(e)
                    return@handleAsync null
                }
                return@handleAsync t
            }
    }
}