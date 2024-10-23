package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Market {
    private Long id;
    private String typeTag;
    private String name;
    private Boolean open;
    private List<Runner> runners;
}
