package lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProductSelectionTest {
    @Test
    fun selectsAvailableProductsInRecommendationOrder() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", false)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)
        val products = listOf(keyboard, mouse, monitor, webcam)

        val result = selectProducts(
            products,
            listOf(3L, 2L, 3L, 99L, 1L, 4L),
            listOf(1L)
        )

        assertEquals(listOf(monitor, webcam), result)
    }

    @Test
    fun returnsEmptyListWhenThereAreNoRecommendations() {
        val products = listOf(Product(1, "Keyboard", true))

        val result = selectProducts(products, emptyList(), emptyList())

        assertEquals(emptyList<Product>(), result)
    }

    @Test
    fun ignoresDuplicateRecommendationsAndExcludedIds() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)

        val result = selectProducts(
            listOf(keyboard, mouse, monitor, webcam),
            listOf(3L, 3L, 2L, 2L, 1L, 4L, 4L),
            listOf(2L, 2L, 999L)
        )

        assertEquals(listOf(monitor, keyboard, webcam), result)
    }

    @Test
    fun handlesEmptyCatalogAndEmptyExclusions() {
        assertEquals(
            emptyList<Product>(),
            selectProducts(emptyList(), listOf(1L, 2L), emptyList())
        )

        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)

        assertEquals(
            listOf(mouse, keyboard),
            selectProducts(listOf(keyboard, mouse), listOf(2L, 1L), emptyList())
        )
    }

    @Test
    fun returnsEmptyListWhenNoProductsMatch() {
        val unavailable = Product(1, "Keyboard", false)
        val excluded = Product(2, "Mouse", true)

        val result = selectProducts(
            listOf(unavailable, excluded),
            listOf(1L, 2L, 99L),
            listOf(2L)
        )

        assertEquals(emptyList<Product>(), result)
    }

    @Test
    fun doesNotMutateInputsAndKeepsCallsIndependent() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)
        val products = mutableListOf(keyboard, mouse)
        val recommendations = mutableListOf(2L, 1L, 2L)
        val exclusions = mutableListOf(1L)

        assertEquals(listOf(mouse), selectProducts(products, recommendations, exclusions))
        assertEquals(listOf(mouse, keyboard), selectProducts(products, recommendations, emptyList()))
        assertEquals(listOf(mouse), selectProducts(products, recommendations, exclusions))

        assertEquals(listOf(keyboard, mouse), products)
        assertEquals(listOf(2L, 1L, 2L), recommendations)
        assertEquals(listOf(1L), exclusions)
    }
}
