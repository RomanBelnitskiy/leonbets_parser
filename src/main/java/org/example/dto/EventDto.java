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
public class EventDto {
    private String caption;
    private List<MarketDto> markets = new ArrayList<>();

    @Override
    public String toString() {
        return caption + "\n"
                + marketsToString();
    }

    private String marketsToString() {
        StringBuilder builder = new StringBuilder();
        for (final MarketDto market : markets) {
            builder
                    .append("\t\t")
                    .append(market.toString());
        }
        return builder.toString();
    }
}
