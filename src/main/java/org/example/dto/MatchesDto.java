package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchesDto {
    private Boolean enabled;
    private Integer totalCount;
    private UUID vtag;
    @Builder.Default
    private List<EventDto> events = new ArrayList<>();
}
