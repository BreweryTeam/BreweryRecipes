package dev.jsinco.recipes.util

import java.nio.ByteBuffer
import java.util.*

object UuidUtil {


    fun toBytes(uuid: UUID): ByteArray {
        val bb = ByteBuffer.allocate(16)
        bb.putLong(uuid.getMostSignificantBits())
        bb.putLong(uuid.getLeastSignificantBits())
        return bb.array()
    }

    fun asUuid(byteArray: ByteArray): UUID {
        val bb = ByteBuffer.wrap(byteArray)
        val firstLong = bb.getLong()
        val secondLong = bb.getLong()
        return UUID(firstLong, secondLong)
    }
}