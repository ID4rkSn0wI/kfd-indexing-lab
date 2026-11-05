# Speed ​​Up Indexing

Selecting available products based on recommendations, while accounting for exclusions and duplicates.

## Getting Started

| File | Purpose |
| --- | --- |
| `src/main/kotlin/lab2/Product.kt` | Data type |
| `src/main/kotlin/lab2/ProductSelection.kt` | Function to be implemented |
| `src/test/kotlin/lab2/ProductSelectionTest.kt` | Tests for the mandatory part |
| `src/main/kotlin/lab2/LimitedProductSelection.kt` | Optional part: index and queries with a limit |
| `src/test/kotlin/lab2/LimitedProductSelectionTest.kt` | Tests for the optional part |
| `README.md` | Instructions for running the code and assignment responses |

Run all tests from the repository root:

```bash
./gradlew test
```

Windows PowerShell:

```powershell
.\gradlew.bat test
```

## Main Assignment

### Solution

**File containing the `selectProducts` implementation:**

`src/main/kotlin/lab2/ProductSelection.kt`

**How ​​does the function work, and why was this data-processing approach chosen?**

The function constructs a `HashMap<Long, Product>` from the available catalog items, removes the excluded IDs, and iterates through the recommendations in order. The `remove(id)` operation simultaneously locates the product and removes it from the temporary index; if an ID appears again, the lookup will return nothing. The identified products are then added to an `ArrayList<Product>`.

**How ​​are the order of recommendations and the uniqueness of the products preserved?** How are excluded, unavailable, and unknown products handled?**

The order is determined by sequentially iterating through the recommendations,
while uniqueness is ensured by removing each identified product from a temporary `Map`.
Excluded items are removed beforehand, unavailable products are never added to the index,
and unknown IDs fail to find a match. The original `Product` objects are returned.

### Complexity

Notation:

- `p` — number of products in the catalog;
- `r` — number of identifiers in the recommendations;
- `e` — number of identifiers in the exclusion list.

**Execution Time:**

**Expected O(p + e + r)**: building the index takes O(p), removing exclusions takes O(e),
and processing recommendations takes O(r). This estimate assumes an average O(1)
time complexity for `HashMap` operations. In the worst case, there is no O(1) guarantee
for individual operations.

**Additional Memory:**

**O(p)** for the temporary `HashMap` containing references to available products.
Additionally, **O(k)** is required for the resulting `ArrayList`, where `k` is the number
of selected products, with `k ≤ min(p, r)`.
Therefore, the total additional memory, including the result, is **O(p + k) = O(p)**.
When an item is removed from the `Map`, its internal table does not necessarily shrink,
while the `ArrayList` simultaneously allocates memory for its own references.
`Product` objects are not copied, and the input collections remain unmodified.

### Tests

The table lists test names or links to them. A single test may verify multiple conditions.

| What is being tested | Tests |
| --- | --- |
| Standard selection; the order of recommendations differs from the order in the catalog | `selectsAvailableProductsInRecommendationOrder`, `preservesRecommendationOrderInsteadOfCatalogOrder` |
| Duplicate recommendations and exclusions | `ignoresDuplicateRecommendationsAndExcludedIds` |
| Unavailable, excluded, and unknown products | `selectsAvailableProductsInRecommendationOrder`, `skipsUnavailableExcludedAndUnknownProducts` |
| Empty catalog, empty recommendations, empty exclusions | `returnsEmptyListWhenCatalogIsEmpty`, `returnsEmptyListWhenThereAreNoRecommendations`, `worksWhenExclusionsAreEmpty` |
| No products match | `returnsEmptyListWhenNoProductsMatch` |
| Inputs remain unchanged; repeated calls are independent | `doesNotMutateInputsAndKeepsCallsIndependent` |

## Optional Follow-up

### Solution Structure

**Implementation files and usage example:**

- `src/main/kotlin/lab2/LimitedProductSelection.kt` — `IndexedProductSelector` class.
- `src/test/kotlin/lab2/LimitedProductSelectionTest.kt` — independent tests for the extension.

```kotlin
val selector = IndexedProductSelector(
listOf(
Product(1, "Keyboard", true),
Product(2, "Mouse", false),
Product(3, "Monitor", true)
)
)
val first = selector.select(
recommendedIds = listOf(3L, 2L, 3L, 1L),
excludedIds = listOf(1L),
limit = 1
) // [Product(id=3, name=Monitor, available=true)]

selector.loadCatalog(listOf(Product(4, "Webcam", true)))
val second = selector.select(
recommendedIds = listOf(3L, 4L),
excludedIds = emptyList(),
limit = 2
) // [Product(id=4, name=Webcam, available=true)]
```

**What happens when the catalog is loaded, and what happens during each individual request? How is request independence ensured, and how is the transition to a new catalog handled?**

Within the constructor and upon calling `loadCatalog(products)`, a separate `HashMap<Long, Product>` is constructed, containing only the available products. During each `select` operation, the master index remains unchanged; instead, a `HashSet<Long>` is created to track excluded IDs and IDs already added to the result set, alongside an `ArrayList<Product>` to store the final results. Since `loadCatalog` completely replaces the index, products from the previous version are no longer included in subsequent requests.

**When does the processing of recommendations stop? What determines the number of items examined?**

If `limit = 0`, an empty list is returned without processing any recommendations. In all other cases, recommendations are scanned from left to right until either the `limit` is reached with unique, valid products, or the end of the list is encountered. The limit check occurs immediately after a product is added, ensuring that the remainder of the list is not read. The number of recommendations examined, `q`, depends on the positions of valid IDs, exclusions, unavailable or unknown products, and the presence of duplicates.

### Complexity

Notation:
- `p` — size of the loaded catalog
- `e` — number of exclusions in a request
- `r` — length of the recommendations list
- `q` — number of recommendations actually read (`q ≤ r`)
- `k` — number of products in the result set (`k ≤ min(p, q, limit)`). Hash operations are assumed to have an average time complexity of O(1).

| Stage | Time | Additional Memory |
| --- | --- | --- |
| Catalog preparation | O(p) | O(p) for the master index |
| Single request | O(e + q) | O(e + k) for exclusions and already-added IDs; the result itself is O(k) |

**Justification of complexity estimates and memory usage for the result:**

A single pass over the catalog is performed only when it is loaded or replaced.
A single query constructs a set of exclusions in O(e) time,
followed by O(1) hash-based checks for each of the `q` recommendations examined.
The set of added IDs contains at most `k` elements.
The result itself is an `ArrayList<Product>` containing `k` references, requiring O(k) memory;
in total, the query requires O(e + k) memory, including the result.
Building the indices requires O(p) time, regardless of the number of queries.
If the limit is zero, the query completes in O(1) time.

**Verification of execution flow**

| Scenario being tested | Tests |
| --- | --- |
| Zero limit | `returnsEmptyWhenLimitIsZeroWithoutReadingRecommendations` |
| Early completion (result filled before exhausting recommendations) | `stopsReadingRecommendationsAsSoonAsLimitIsReached`, `skipsRejectedRecommendationsAndThenStopsAtLimit` |
| Fewer matching products than the limit | `returnsAllMatchingProductsWhenLimitIsLargerThanResult`, `returnsEmptyListWhenNoRecommendationsQualify` |
| Multiple independent queries on the same catalog | `requestsRemainIndependentAndInputsAreUnchanged`, `changingOriginalCatalogListDoesNotChangeLoadedIndex` |
| Switching to a new catalog | `reloadingCatalogReplacesAllPreviousProducts`, `changingOriginalCatalogListDoesNotChangeLoadedIndex` |

Additionally, the implementation is verified against cases involving unknown, unavailable, or excluded products, duplicates,
an empty catalog, and empty recommendation lists. **How ​​is the early termination confirmed?**

The test `stopsReadingRecommendationsAsSoonAsLimitIsReached` provides **1,000 recommendations**—the first two of which are valid—with a **`limit = 2`**.
A custom `CountingRecommendations : AbstractList<Long>` class increments a counter within its `get(index)` method;
upon execution, exactly **2 elements are read**, and the result contains both expected, fully-populated `Product` objects.
An additional test, `skipsRejectedRecommendationsAndThenStopsAtLimit`, demonstrates that when the input includes unknown, unavailable, excluded, or duplicate IDs,
the process reads 6 out of the 7 available recommendations before reaching the specified limit.
