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
public class RegionDto {
    private Long id;
    private String name;
    private String nameDefault;
    private String family;
    @Builder.Default
    private List<LeagueDto> leagues = new ArrayList<>();
}
