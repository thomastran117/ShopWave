package backend.services.impl;

import backend.dtos.responses.forecasting.ForecastSummaryResponse;
import backend.dtos.responses.forecasting.ProductForecastResponse;
import backend.exceptions.http.ForbiddenException;
import backend.models.core.Company;
import backend.models.core.Product;
import backend.repositories.CompanyRepository;
import backend.repositories.ProductRepository;
import backend.services.intf.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForecastingServiceImplTest {

    @Mock CompanyRepository companyRepository;
    @Mock ProductRepository productRepository;
    @Mock CacheService cacheService;

    private ForecastingServiceImpl service;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        service = new ForecastingServiceImpl(companyRepository, productRepository, cacheService, mapper);
    }

    // -------------------------------------------------------------------------
    // Ownership enforcement
    // -------------------------------------------------------------------------

    @Test
    void getCompanyForecast_nonOwner_throwsForbidden() {
        when(companyRepository.findByIdAndOwnerId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCompanyForecast(1L, 99L, 56, 50))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getProductForecast_nonOwner_throwsForbidden() {
        when(companyRepository.findByIdAndOwnerId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductForecast(1L, 42L, 99L, 56))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getReorderSuggestions_nonOwner_throwsForbidden() {
        when(companyRepository.findByIdAndOwnerId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReorderSuggestions(1L, 99L, 56, 20))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getSeasonalPrep_nonOwner_throwsForbidden() {
        when(companyRepository.findByIdAndOwnerId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSeasonalPrep(1L, 99L, 50))
                .isInstanceOf(ForbiddenException.class);
    }

    // -------------------------------------------------------------------------
    // Cache hit — company forecast
    // -------------------------------------------------------------------------

    @Test
    void getCompanyForecast_cacheHit_doesNotQueryDb() throws Exception {
        when(companyRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(new Company()));

        ForecastSummaryResponse cached = new ForecastSummaryResponse(
                1L, 56, Instant.now(), 0, 0, 0, List.of());
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        when(cacheService.get(anyString())).thenReturn(mapper.writeValueAsString(cached));

        ForecastSummaryResponse result = service.getCompanyForecast(1L, 1L, 56, 50);

        assertThat(result.companyId()).isEqualTo(1L);
        verify(productRepository, never()).findDailyDemandSince(anyLong(), any());
    }

    // -------------------------------------------------------------------------
    // Cache miss — company forecast happy path
    // -------------------------------------------------------------------------

    @Test
    void getCompanyForecast_cacheMiss_returnsCorrectShape() {
        when(companyRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(new Company()));
        when(cacheService.get(anyString())).thenReturn(null);
        when(productRepository.findDailyDemandSince(eq(1L), any())).thenReturn(List.of());
        when(productRepository.findAllByCompanyId(1L)).thenReturn(List.of(makeProduct(10L, 50)));

        ForecastSummaryResponse result = service.getCompanyForecast(1L, 1L, 56, 50);

        assertThat(result.companyId()).isEqualTo(1L);
        assertThat(result.windowDays()).isEqualTo(56);
        assertThat(result.items()).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // Zero-sales product
    // -------------------------------------------------------------------------

    @Test
    void getCompanyForecast_noSales_productHasZeroDemand() {
        when(companyRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(new Company()));
        when(cacheService.get(anyString())).thenReturn(null);
        when(productRepository.findDailyDemandSince(eq(1L), any())).thenReturn(List.of());
        when(productRepository.findAllByCompanyId(1L)).thenReturn(List.of(makeProduct(10L, 5)));

        ForecastSummaryResponse result = service.getCompanyForecast(1L, 1L, 56, 50);

        ProductForecastResponse item = result.items().get(0);
        assertThat(item.avgDailyDemand()).isEqualTo(0.0);
        assertThat(item.likelyStockoutDate()).isNull();
        assertThat(item.reorderSuggestedQty()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Reorder urgency
    // -------------------------------------------------------------------------

    @Test
    void getCompanyForecast_lowStockBelowThreshold_flaggedUrgent() {
        when(companyRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(new Company()));
        when(cacheService.get(anyString())).thenReturn(null);

        // Product with stock=2, lowStockThreshold=5 → should be flagged urgent
        Product p = makeProduct(10L, 2);
        p.setLowStockThreshold(5);
        when(productRepository.findAllByCompanyId(1L)).thenReturn(List.of(p));
        when(productRepository.findDailyDemandSince(eq(1L), any())).thenReturn(List.of());

        ForecastSummaryResponse result = service.getCompanyForecast(1L, 1L, 56, 50);

        assertThat(result.items().get(0).reorderUrgent()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Seasonal prep — insufficient history
    // -------------------------------------------------------------------------

    @Test
    void getSeasonalPrep_noYoYData_returnsInsufficientHistory() {
        when(companyRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(new Company()));
        when(productRepository.findDailyDemandBetween(eq(1L), any(), any())).thenReturn(List.of());

        var result = service.getSeasonalPrep(1L, 1L, 50);

        assertThat(result.items()).isEmpty();
        assertThat(result.reason()).isEqualTo("INSUFFICIENT_HISTORY");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Product makeProduct(long id, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName("Product " + id);
        p.setSku("SKU-" + id);
        p.setStock(stock);
        p.setAutoRestockEnabled(false);
        return p;
    }

}
