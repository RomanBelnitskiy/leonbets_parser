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
public class Event {
    private String caption;
    private List<Market> markets = new ArrayList<>();

    @Override
    public String toString() {
        return caption + "\n"
                + marketsToString();
    }

    private String marketsToString() {
        StringBuilder builder = new StringBuilder();
        for (final Market market : markets) {
            builder
                    .append("\t\t")
                    .append(market.toString());
        }
        return builder.toString();
    }
}
