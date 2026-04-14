package backend.services.impl;

import backend.models.core.Product;
import backend.models.core.ProductBundle;

/**
 * Sealed work-item type submitted to the ProductIndexingService queue.
 * Each variant represents one indexing or removal operation for a product or bundle.
 */
sealed interface IndexingTask
        permits IndexingTask.IndexProduct, IndexingTask.RemoveProduct,
                IndexingTask.IndexBundle, IndexingTask.RemoveBundle {

    /**
     * companyId is captured from the entity while it is still loaded in the calling
     * JPA session — this prevents LazyInitializationException in worker threads.
     */
    record IndexProduct(Product product, long companyId) implements IndexingTask {}

    record RemoveProduct(long productId) implements IndexingTask {}

    record IndexBundle(ProductBundle bundle) implements IndexingTask {}

    record RemoveBundle(long bundleId) implements IndexingTask {}
}
