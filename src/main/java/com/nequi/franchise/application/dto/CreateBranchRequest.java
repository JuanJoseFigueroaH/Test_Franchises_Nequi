package com.nequi.franchise.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBranchRequest {
    @NotBlank(message = "Franchise ID is required")
    private String franchiseId;

    @NotBlank(message = "Name is required")
    private String name;
}
