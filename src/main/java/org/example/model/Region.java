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
public class Region {
    private Long id;
    private String name;
    private String nameDefault;
    private String family;
    @Builder.Default
    private List<League> leagues = new ArrayList<>();
}
