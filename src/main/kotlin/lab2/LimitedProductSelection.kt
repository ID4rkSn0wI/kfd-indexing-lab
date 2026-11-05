package lab2

/**
 * Stores the catalog index for future requests.
 * Only available products are indexed.
 * Map can't be modified
 */
class IndexedProductSelector(products: List<Product>) {
    private var availableById: Map<Long, Product> = emptyMap()

    init {
        loadCatalog(products)
    }

    /** Indexing previous catalog. */
    fun loadCatalog(products: List<Product>) {
        val newIndex = HashMap<Long, Product>(products.size)
        for (product in products) {
            if (product.available) {
                newIndex[product.id] = product
            }
        }
        availableById = newIndex
    }

    /** Selects the first stated needed products in recommendation order. */
    fun select(
        recommendedIds: List<Long>,
        excludedIds: List<Long>,
        limit: Int
    ): List<Product> {
        if (limit == 0 || recommendedIds.isEmpty() || availableById.isEmpty()) {
            return emptyList()
        }

        val excludedSet = HashSet<Long>(excludedIds.size)
        for (id in excludedIds) {
            excludedSet.add(id)
        }

        val addedIds = HashSet<Long>()
        val result = ArrayList<Product>()
        for (id in recommendedIds) {
            if (id in excludedSet) continue
            val product = availableById[id] ?: continue
            if (addedIds.add(id)) {
                result.add(product)
                if (result.size == limit) break
            }
        }
        return result
    }
}
