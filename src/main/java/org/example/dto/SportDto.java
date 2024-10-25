package org.example.dto;

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
public class SportDto {
    private Long id;
    private String name;
    private Integer weight;
    private String family;
    @Builder.Default
    private List<RegionDto> regions = new ArrayList<>();
}
