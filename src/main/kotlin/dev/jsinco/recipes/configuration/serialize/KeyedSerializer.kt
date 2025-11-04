package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.util.Logger
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.Keyed

class KeyedSerializer<T : Keyed>(private val registryKey: RegistryKey<T>, private val tClass: Class<T>) :
    ObjectSerializer<T> {
    override fun supports(type: Class<in T>): Boolean {
        return tClass.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: T,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        data.setValue(`object`.key)
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): T? {
        val key = data.getValue(Key::class.java) ?: let {
            Logger.logErr("Invalid key: ${data.valueRaw}")
            return null
        }
        return RegistryAccess.registryAccess().getRegistry(registryKey).get(key) ?: let {
            Logger.logErr("Unknown key: $key")
            null
        }
    }
}