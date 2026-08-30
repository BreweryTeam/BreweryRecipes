package dev.jsinco.recipes.util.ext

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IterableExtTest {

    @Test
    fun `removeAdjacentWhere returns empty list for empty input`() {
        assertEquals(emptyList<Int>(), emptyList<Int>().removeAdjacentWhere { it % 2 == 0 })
    }

    @Test
    fun `removeAdjacentWhere returns single element for single input`() {
        assertEquals(listOf(1), listOf(1).removeAdjacentWhere { it % 2 == 0 })
    }

    @Test
    fun `removeAdjacentWhere returns all items when none satisfy predicate`() {
        val input = listOf(1, 3, 5)
        assertEquals(input, input.removeAdjacentWhere { it % 2 == 0 })
    }

    @Test
    fun `removeAdjacentWhere keeps first element when all satisfy predicate`() {
        val input = listOf(2, 4, 6)
        assertEquals(listOf(2), input.removeAdjacentWhere { it % 2 == 0 })
    }

    @Test
    fun `removeAdjacentWhere removes adjacent even numbers`() {
        val input = listOf(1, 2, 4, 3, 6, 8, 5)
        assertEquals(listOf(1, 2, 3, 6, 5), input.removeAdjacentWhere { it % 2 == 0 })
    }

    @Test
    fun `removeAdjacentWhere keeps isolated even numbers`() {
        val input = listOf(1, 2, 3, 4, 5)
        assertEquals(input, input.removeAdjacentWhere { it % 2 == 0 })
    }

    @Test
    fun `removeAdjacentWhere works with custom predicate`() {
        val input = listOf(1, "a", 2, "b", 3, "a")
        assertEquals(listOf(1, "a", 2, "b", 3, "a"), input.removeAdjacentWhere { it is Int })
    }

    @Test
    fun `removeAdjacentWhere works with null values`() {
        val input = listOf(1, null, 2, null, 3, null)
        assertEquals(listOf(1, null, 2, null, 3, null), input.removeAdjacentWhere { it == null })
    }

    @Test
    fun `removeAdjacentWhere keeps string values when both odd numbers satisfy predicate`() {
        val input = listOf(1, 2, "a", 3, "b", 4)
        assertEquals(listOf(1, "a", 3, "b", 4), input.removeAdjacentWhere { it is Int })
    }

}
