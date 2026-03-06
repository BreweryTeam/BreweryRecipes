package dev.jsinco.recipes.util.metadata

import dev.jsinco.brewery.api.meta.MetaDataType
import dev.jsinco.recipes.util.UuidUtil
import java.util.*

object UuidMetaDataType : MetaDataType<ByteArray, UUID> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<UUID> {
        return UUID::class.java
    }

    override fun toPrimitive(complex: UUID): ByteArray {
        return UuidUtil.toBytes(complex)
    }

    override fun toComplex(primitive: ByteArray): UUID {
        return UuidUtil.asUuid(primitive)
    }
}