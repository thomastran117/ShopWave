package backend.dtos.responses.general;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PagedResponse<T> {
    private final List<T> items;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PagedResponse(Page<T> springPage) {
        this.items = springPage.getContent();
        this.page = springPage.getNumber();
        this.size = springPage.getSize();
        this.totalElements = springPage.getTotalElements();
        this.totalPages = springPage.getTotalPages();
        this.hasNext = springPage.hasNext();
        this.hasPrevious = springPage.hasPrevious();
    }
}
