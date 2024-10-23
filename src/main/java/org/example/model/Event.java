package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private Long id;
    private String name;
    private String nameDefault;
    private Date kickoff;
    @Builder.Default
    private List<Market> markets = new ArrayList<>();
}
