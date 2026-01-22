package com.nequi.franchise.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Page<T> {
    private List<T> items;
    private String nextCursor;
    private Integer pageSize;
    private Boolean hasMore;

    public static <T> Page<T> of(List<T> items, String nextCursor, Integer pageSize) {
        return Page.<T>builder()
                .items(items)
                .nextCursor(nextCursor)
                .pageSize(items.size())
                .hasMore(nextCursor != null)
                .build();
    }

    public static <T> Page<T> empty() {
        return Page.<T>builder()
                .items(List.of())
                .nextCursor(null)
                .pageSize(0)
                .hasMore(false)
                .build();
    }
}
