package lab2


/*
* Index products by their availability, removes excluded and then makes recommendation list
*/
fun selectProducts(
    products: List<Product>,
    recommendedIds: List<Long>,
    excludedIds: List<Long>
): List<Product> {
    if (products.isEmpty() || recommendedIds.isEmpty()) return emptyList()

    /* Index products */
    val availableById = HashMap<Long, Product>(products.size)
    for (product in products) {
        if (product.available) {
            availableById[product.id] = product
        }
    }

    /* Excludes products */
    for (excludedId in excludedIds) {
        availableById.remove(excludedId)
    }
    /* Makes recommendation list */
    val result = ArrayList<Product>()
    for (recommendedId in recommendedIds) {
        val product = availableById.remove(recommendedId)
        if (product != null) {
            result.add(product)
        }
    }

    return result
}
