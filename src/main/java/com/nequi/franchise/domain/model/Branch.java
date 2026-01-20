package com.nequi.franchise.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branch {
    private String id;
    private String name;
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
