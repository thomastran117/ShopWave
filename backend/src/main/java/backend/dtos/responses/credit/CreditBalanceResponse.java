package backend.dtos.responses.credit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreditBalanceResponse {
    private Long userId;
    private long balanceCents;
    private String currency;
    private List<CreditEntryResponse> entries;
}
