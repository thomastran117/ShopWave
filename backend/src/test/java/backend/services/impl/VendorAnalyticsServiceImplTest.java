package backend.services.impl;

import backend.dtos.responses.vendor.VendorPayoutsMetricResponse;
import backend.exceptions.http.ForbiddenException;
import backend.models.core.Company;
import backend.models.core.MarketplaceProfile;
import backend.models.core.MarketplaceVendor;
import backend.models.core.User;
import backend.models.enums.PayoutStatus;
import backend.repositories.MarketplaceProfileRepository;
import backend.repositories.MarketplaceVendorRepository;
import backend.repositories.VendorAnalyticsRepository;
import backend.repositories.VendorPayoutRepository;
import backend.services.impl.vendors.VendorAnalyticsServiceImpl;
import backend.services.intf.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VendorAnalyticsServiceImplTest {

    private VendorAnalyticsRepository analyticsRepository;
    private VendorPayoutRepository payoutRepository;
    private MarketplaceProfileRepository marketplaceProfileRepository;
    private MarketplaceVendorRepository marketplaceVendorRepository;
    private CacheService cacheService;
    private VendorAnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        analyticsRepository = mock(VendorAnalyticsRepository.class);
        payoutRepository = mock(VendorPayoutRepository.class);
        marketplaceProfileRepository = mock(MarketplaceProfileRepository.class);
        marketplaceVendorRepository = mock(MarketplaceVendorRepository.class);
        cacheService = mock(CacheService.class);

        service = new VendorAnalyticsServiceImpl(
                analyticsRepository,
                payoutRepository,
                marketplaceProfileRepository,
                marketplaceVendorRepository,
                cacheService,
                new ObjectMapper());
    }

    @Test
    void getSummary_throwsWhenActorCannotAccessVendor() {
        MarketplaceVendor vendor = makeVendor(7L, 20L, 10L);
        when(marketplaceVendorRepository.findById(7L)).thenReturn(Optional.of(vendor));
        when(marketplaceProfileRepository.findByCompanyId(20L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.getSummary(7L, 20L, 30, 99L));
    }

    @Test
    void getPayouts_allowsMarketplaceOperator() {
        MarketplaceVendor vendor = makeVendor(7L, 20L, 10L);
        MarketplaceProfile profile = makeMarketplaceProfile(20L, 55L);

        when(marketplaceVendorRepository.findById(7L)).thenReturn(Optional.of(vendor));
        when(marketplaceProfileRepository.findByCompanyId(20L)).thenReturn(Optional.of(profile));
        when(payoutRepository.findByVendorIdAndStatus(eq(7L), eq(PayoutStatus.PAID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        VendorPayoutsMetricResponse response = service.getPayouts(7L, 20L, 10, 55L);

        assertEquals(7L, response.getVendorId());
        verify(payoutRepository).findByVendorIdAndStatus(eq(7L), eq(PayoutStatus.PAID), any(Pageable.class));
    }

    private MarketplaceVendor makeVendor(long vendorId, long marketplaceId, long vendorOwnerId) {
        User owner = new User();
        owner.setId(vendorOwnerId);

        Company vendorCompany = new Company();
        vendorCompany.setId(300L);
        vendorCompany.setOwner(owner);

        Company marketplaceCompany = new Company();
        marketplaceCompany.setId(marketplaceId);

        MarketplaceVendor vendor = new MarketplaceVendor();
        vendor.setId(vendorId);
        vendor.setVendorCompany(vendorCompany);
        vendor.setMarketplace(marketplaceCompany);
        return vendor;
    }

    private MarketplaceProfile makeMarketplaceProfile(long marketplaceId, long operatorUserId) {
        User operator = new User();
        operator.setId(operatorUserId);

        Company marketplaceCompany = new Company();
        marketplaceCompany.setId(marketplaceId);
        marketplaceCompany.setOwner(operator);

        MarketplaceProfile profile = new MarketplaceProfile();
        profile.setId(500L);
        profile.setCompany(marketplaceCompany);
        return profile;
    }
}
