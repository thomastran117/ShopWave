package backend.events;

import backend.models.core.Product;

public record ProductIndexEvent(Product product, long companyId) {}
