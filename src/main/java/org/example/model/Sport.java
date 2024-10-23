package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sport {
    private Long id;
    private String name;
    private Integer weight;
    private String family;
    @Builder.Default
    private List<Region> regions = new ArrayList<>();
}
