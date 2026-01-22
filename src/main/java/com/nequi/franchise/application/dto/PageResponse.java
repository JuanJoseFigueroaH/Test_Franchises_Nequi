package com.nequi.franchise.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {

    @Schema(description = "List of items in current page")
    private List<T> items;

    @Schema(description = "Total number of items in current page")
    private Integer pageSize;

    @Schema(description = "Cursor for next page (null if no more pages)", example = "eyJpZCI6ImZyYW5jaGlzZS0xMjMifQ==")
    private String nextCursor;

    @Schema(description = "Indicates if there are more pages available")
    private Boolean hasMore;

    public static <T> PageResponse<T> of(List<T> items, String nextCursor, Integer pageSize) {
        return PageResponse.<T>builder()
                .items(items)
                .pageSize(items.size())
                .nextCursor(nextCursor)
                .hasMore(nextCursor != null)
                .build();
    }

    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .items(List.of())
                .pageSize(0)
                .nextCursor(null)
                .hasMore(false)
                .build();
    }
}
