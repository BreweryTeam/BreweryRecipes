package dev.jsinco.recipes.configuration.serialize

import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import io.papermc.paper.datacomponent.item.CustomModelData
import org.bukkit.Color

object CustomModelDataSerializer : ObjectSerializer<CustomModelData> {

    override fun supports(type: Class<*>): Boolean {
        return CustomModelData::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: CustomModelData,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        if (`object`.floats().size == 1
            && `object`.flags().isEmpty()
            && `object`.strings().isEmpty()
            && `object`.colors().isEmpty()
        ) {
            val f = `object`.floats().first()
            val i = f.toInt()
            if (i.toFloat() == f) {
                data.setValue(i)
                return
            }
        }

        data.addElementOrList("floats", `object`.floats())
        data.addElementOrList("flags", `object`.flags())
        data.addElementOrList("strings", `object`.strings())
        data.addElementOrList("colors", `object`.colors())
    }

    private fun <T> SerializationData.addElementOrList(name: String, list: List<T>) {
        when (list.size) {
            0 -> {}
            1 -> add(name, list.first())
            else -> add(name, list)
        }
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): CustomModelData? {
        if (data.containsKey(ObjectSerializer.VALUE)) {
            return CustomModelData.customModelData()
                .addFloat(data.getValue(Float::class.java))
                .build()
        }

        return CustomModelData.customModelData().apply {
            var modified = false

            data.getElementOrList<Float>("floats")?.let {
                addFloats(it)
                modified = true
            }
            data.getElementOrList<Boolean>("flags")?.let {
                addFlags(it)
                modified = true
            }
            data.getElementOrList<String>("strings")?.let {
                addStrings(it)
                modified = true
            }
            data.getElementOrList<Color>("colors")?.let {
                addColors(it)
                modified = true
            }

            if (!modified) {
                return null
            }
        }.build()
    }

    private inline fun <reified T> DeserializationData.getElementOrList(name: String): List<T>? {
        val value = get(name, Any::class.java)
        return when (value) {
            is List<*> -> getAsList(name, T::class.java)
            is Any -> get(name, T::class.java)?.let { return listOf(it) }
            else -> null
        }
    }

}
