package dev.jsinco.recipes.util.ext

/**
 * Like [distinctBy], but any duplicate items that satisfy the [predicate] are not removed.
 */
inline fun <T, K> Iterable<T>.distinctByExcept(selector: (T) -> K, predicate: (T) -> Boolean): List<T> {
    return this.distinctBy {
        if (predicate(it)) {
            Any() // Any() instances are never equal to each other
        } else {
            selector(it)
        }
    }
}

/**
 * Removes adjacent items for which their [selector] values are equal.
 */
inline fun <T, K> Iterable<T>.distinctByUntilChanged(selector: (T) -> K): List<T> {
    val iterator = this.iterator()
    if (!iterator.hasNext()) return emptyList()
    val initial = iterator.next()
    var accumulator = selector(initial)
    val result = ArrayList<T>().apply { add(initial) }
    while (iterator.hasNext()) {
        val next = iterator.next()
        val toCompare = selector(next)
        if (toCompare != accumulator) {
            result.add(next)
            accumulator = toCompare
        }
    }
    return result
}
