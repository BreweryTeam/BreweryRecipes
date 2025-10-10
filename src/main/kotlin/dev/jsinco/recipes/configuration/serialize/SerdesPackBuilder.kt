package dev.jsinco.recipes.configuration.serialize

import com.google.common.collect.ImmutableList
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.OkaeriSerdesPack
import eu.okaeri.configs.serdes.SerdesRegistry
import java.util.function.Consumer

class SerdesPackBuilder {
    var objectSerializers: ImmutableList.Builder<ObjectSerializer<*>?> = ImmutableList.Builder<ObjectSerializer<*>?>()

    fun add(objectSerializer: ObjectSerializer<*>) = apply {
        objectSerializers.add(objectSerializer)
    }

    fun build(): OkaeriSerdesPack {
        return OkaeriSerdesPackImpl(objectSerializers.build())
    }

    @JvmRecord
    private data class OkaeriSerdesPackImpl(val serializers: MutableList<ObjectSerializer<*>?>?) : OkaeriSerdesPack {
        override fun register(registry: SerdesRegistry) {
            serializers!!.forEach(Consumer { serializer: ObjectSerializer<*>? -> registry.register(serializer!!) })
        }
    }
}