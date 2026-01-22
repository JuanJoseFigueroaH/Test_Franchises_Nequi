package com.nequi.franchise.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination request parameters")
public class PageRequest {

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(description = "Number of items per page", example = "20", defaultValue = "20")
    private Integer pageSize;

    @Schema(description = "Cursor for next page (base64 encoded)", example = "eyJpZCI6ImZyYW5jaGlzZS0xMjMifQ==")
    private String cursor;

    public Integer getPageSize() {
        return pageSize != null ? pageSize : 20;
    }
}
