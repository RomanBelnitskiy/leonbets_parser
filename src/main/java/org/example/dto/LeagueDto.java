package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeagueDto {
    private Long id;
    private String name;
    private String nameDefault;
    private Boolean top;
    private Integer topOrder;
    private String regionName;
}
