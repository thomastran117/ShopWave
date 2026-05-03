package backend.services.impl;

import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.sla.VendorSLAMetricResponse;
import backend.exceptions.http.ForbiddenException;
import backend.models.core.Company;
import backend.models.core.MarketplaceProfile;
import backend.models.core.MarketplaceVendor;
import backend.models.core.User;
import backend.models.core.VendorSLAMetric;
import backend.repositories.MarketplaceProfileRepository;
import backend.repositories.MarketplaceVendorRepository;
import backend.repositories.VendorSLABreachRepository;
import backend.repositories.VendorSLAMetricRepository;
import backend.repositories.VendorSLAPolicyRepository;
import backend.services.impl.vendors.VendorSLAServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
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
class VendorSLAServiceImplTest {

    private VendorSLAPolicyRepository policyRepository;
    private VendorSLAMetricRepository metricRepository;
    private VendorSLABreachRepository breachRepository;
    private MarketplaceProfileRepository marketplaceProfileRepository;
    private MarketplaceVendorRepository marketplaceVendorRepository;
    private VendorSLAServiceImpl service;

    @BeforeEach
    void setUp() {
        policyRepository = mock(VendorSLAPolicyRepository.class);
        metricRepository = mock(VendorSLAMetricRepository.class);
        breachRepository = mock(VendorSLABreachRepository.class);
        marketplaceProfileRepository = mock(MarketplaceProfileRepository.class);
        marketplaceVendorRepository = mock(MarketplaceVendorRepository.class);

        service = new VendorSLAServiceImpl(
                policyRepository,
                metricRepository,
                breachRepository,
                marketplaceProfileRepository,
                marketplaceVendorRepository);
    }

    @Test
    void listMetrics_throwsWhenActorCannotAccessVendor() {
        MarketplaceVendor vendor = makeVendor(7L, 20L, 10L);
        when(marketplaceVendorRepository.findById(7L)).thenReturn(Optional.of(vendor));
        when(marketplaceProfileRepository.findByCompanyId(20L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.listMetrics(20L, 7L, 99L, 0, 10));
    }

    @Test
    void listMetrics_allowsVendorOwner() {
        MarketplaceVendor vendor = makeVendor(7L, 20L, 10L);
        VendorSLAMetric metric = new VendorSLAMetric();
        metric.setId(1L);
        metric.setVendorId(7L);
        metric.setMarketplaceId(20L);
        metric.setDate(LocalDate.now());
        metric.setTotalOrders(12);

        when(marketplaceVendorRepository.findById(7L)).thenReturn(Optional.of(vendor));
        when(metricRepository.findByVendorId(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(metric)));

        PagedResponse<VendorSLAMetricResponse> response = service.listMetrics(20L, 7L, 10L, 0, 10);

        assertEquals(1, response.getItems().size());
        assertEquals(7L, response.getItems().get(0).getVendorId());
        verify(metricRepository).findByVendorId(eq(7L), any(Pageable.class));
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
