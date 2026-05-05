package backend.dtos.responses.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class BundleResponse {
    private Long id;
    private Long companyId;
    private String name;
    private String description;
    private String thumbnailUrl;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String currency;
    private String status;
    private boolean listed;
    private List<BundleItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;
}
