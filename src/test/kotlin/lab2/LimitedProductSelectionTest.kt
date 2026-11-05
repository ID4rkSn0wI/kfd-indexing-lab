package lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LimitedProductSelectionTest {
    @Test
    fun returnsEmptyWhenLimitIsZeroWithoutReadingRecommendations() {
        val selector = IndexedProductSelector(listOf(Product(1, "Keyboard", true)))
        val recommendations = CountingRecommendations(listOf(1L, 1L, 1L))

        assertEquals(emptyList<Product>(), selector.select(recommendations, emptyList(), limit = 0))
        assertEquals(0, recommendations.readCount)
    }

    @Test
    fun stopsReadingRecommendationsAsSoonAsLimitIsReached() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", false)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)
        val selector = IndexedProductSelector(listOf(keyboard, mouse, monitor, webcam))
        val recommendations = CountingRecommendations(
            listOf(99L, 2L, 1L, 3L, 3L, 4L) + List(994) { 4L }
        )

        val result = selector.select(
            recommendedIds = recommendations,
            excludedIds = listOf(1L, 1L, 100L),
            limit = 2
        )

        assertEquals(listOf(monitor, webcam), result)
        assertEquals(1000, recommendations.size)
        assertEquals(6, recommendations.readCount)
    }

    @Test
    fun returnsAllMatchingProductsWhenLimitIsLargerThanResult() {
        val keyboard = Product(1, "Keyboard", true)
        val monitor = Product(3, "Monitor", true)
        val selector = IndexedProductSelector(listOf(keyboard, monitor))

        val result = selector.select(listOf(3L, 999L, 3L, 1L), emptyList(), limit = 10)

        assertEquals(listOf(monitor, keyboard), result)
    }

    @Test
    fun requestsRemainIndependentAndInputsAreUnchanged() {
        val keyboard = Product(1, "Keyboard", true)
        val monitor = Product(3, "Monitor", true)
        val catalog = mutableListOf(keyboard, monitor)
        val recommendations = mutableListOf(3L, 1L, 3L)
        val exclusions = mutableListOf(1L)
        val selector = IndexedProductSelector(catalog)

        assertEquals(listOf(monitor), selector.select(recommendations, exclusions, 5))
        assertEquals(listOf(monitor, keyboard), selector.select(recommendations, emptyList(), 5))
        assertEquals(listOf(monitor), selector.select(recommendations, exclusions, 5))
        assertEquals(listOf(keyboard, monitor), catalog)
        assertEquals(listOf(3L, 1L, 3L), recommendations)
        assertEquals(listOf(1L), exclusions)
    }

    @Test
    fun reloadingCatalogReplacesAllPreviousProducts() {
        val keyboard = Product(1, "Keyboard", true)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)
        val selector = IndexedProductSelector(listOf(keyboard, monitor))

        assertEquals(listOf(monitor, keyboard), selector.select(listOf(3L, 1L), emptyList(), 5))
        selector.loadCatalog(listOf(webcam))
        assertEquals(listOf(webcam), selector.select(listOf(3L, 4L, 1L), emptyList(), 5))
    }

    private class CountingRecommendations(private val ids: List<Long>) : AbstractList<Long>() {
        var readCount: Int = 0
            private set

        override val size: Int
            get() = ids.size

        override fun get(index: Int): Long {
            readCount++
            return ids[index]
        }
    }
}
