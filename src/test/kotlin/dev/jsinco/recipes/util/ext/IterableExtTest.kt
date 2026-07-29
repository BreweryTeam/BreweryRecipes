package dev.jsinco.recipes.util.ext

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IterableExtTest {

    @Test
    fun `distinctByExcept returns empty list for empty input`() {
        assertEquals(emptyList<Int>(), emptyList<Int>().distinctByExcept({ it }, { false }))
    }

    @Test
    fun `distinctByExcept returns all items when no duplicates`() {
        val input = listOf(1, 2, 3)
        assertEquals(input, input.distinctByExcept({ it }, { false }))
    }

    @Test
    fun `distinctByExcept removes duplicates when predicate is never true`() {
        val input = listOf(1, 2, 1, 3, 2, 3)
        assertEquals(listOf(1, 2, 3), input.distinctByExcept({ it }, { false }))
    }

    @Test
    fun `distinctByExcept keeps all items satisfying predicate`() {
        val input = listOf(1, 2, 1, 2, 1)
        assertEquals(listOf(1, 2, 1, 2, 1), input.distinctByExcept({ it }, { true }))
    }

    @Test
    fun `distinctByExcept keeps predicate items but removes non-predicate duplicates`() {
        val input = listOf(1, 2, 1, 3, 2, 4)
        assertEquals(listOf(1, 2, 3, 2, 4), input.distinctByExcept({ it }, { it == 2 }))
    }

    @Test
    fun `distinctByExcept works with selector different from identity`() {
        data class Item(val id: Int, val group: String)
        val input = listOf(
            Item(1, "a"),
            Item(2, "a"),
            Item(3, "b"),
            Item(4, "b")
        )
        val result = input.distinctByExcept({ it.group }, { it.id == 2 })
        assertEquals(listOf(Item(1, "a"), Item(2, "a"), Item(3, "b")), result)
    }

    @Test
    fun `distinctByUntilChanged returns empty list for empty input`() {
        assertEquals(emptyList<Int>(), emptyList<Int>().distinctByUntilChanged { it })
    }

    @Test
    fun `distinctByUntilChanged returns single element for single input`() {
        assertEquals(listOf(1), listOf(1).distinctByUntilChanged { it })
    }

    @Test
    fun `distinctByUntilChanged returns all items when no consecutive duplicates`() {
        val input = listOf(1, 2, 3, 4)
        assertEquals(input, input.distinctByUntilChanged { it })
    }

    @Test
    fun `distinctByUntilChanged removes consecutive duplicates`() {
        val input = listOf(1, 1, 1, 2, 2, 3)
        assertEquals(listOf(1, 2, 3), input.distinctByUntilChanged { it })
    }

    @Test
    fun `distinctByUntilChanged keeps non-consecutive duplicates`() {
        val input = listOf(1, 2, 1, 2, 1)
        assertEquals(input, input.distinctByUntilChanged { it })
    }

    @Test
    fun `distinctByUntilChanged returns only first element when all same`() {
        val input = listOf(1, 1, 1, 1)
        assertEquals(listOf(1), input.distinctByUntilChanged { it })
    }

    @Test
    fun `distinctByUntilChanged works with selector different from identity`() {
        data class Item(val id: Int, val group: String)
        val input = listOf(
            Item(1, "a"),
            Item(2, "a"),
            Item(3, "b"),
            Item(4, "a")
        )
        val result = input.distinctByUntilChanged { it.group }
        assertEquals(listOf(Item(1, "a"), Item(3, "b"), Item(4, "a")), result)
    }

}
