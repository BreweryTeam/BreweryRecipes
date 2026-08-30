package dev.jsinco.recipes.util.ext

/**
 * Removes adjacent elements when their [predicate] values are both true.
 */
inline fun <T> Iterable<T>.removeAdjacentWhere(predicate: (T) -> Boolean): List<T> {
    val iterator = this.iterator()
    if (!iterator.hasNext()) return emptyList()
    val initial = iterator.next()
    var prevPredicate = predicate(initial)
    val result = ArrayList<T>().apply { add(initial) }
    while (iterator.hasNext()) {
        val next = iterator.next()
        val currentPredicate = predicate(next)
        if (!prevPredicate || !currentPredicate) {
            result.add(next)
        }
        prevPredicate = currentPredicate
    }
    return result
}
