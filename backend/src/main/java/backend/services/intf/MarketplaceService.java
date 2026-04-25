package backend.services.intf;

import backend.dtos.requests.marketplace.CreateMarketplaceRequest;
import backend.dtos.requests.marketplace.UpdateMarketplaceRequest;
import backend.dtos.responses.marketplace.MarketplaceProfileResponse;

public interface MarketplaceService {

    MarketplaceProfileResponse createMarketplace(long ownerId, long companyId, CreateMarketplaceRequest request);

    MarketplaceProfileResponse getMarketplace(long marketplaceId);

    MarketplaceProfileResponse updateMarketplace(long marketplaceId, long ownerId, UpdateMarketplaceRequest request);
}
