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
public class League {
    private Long id;
    private String caption;
    private String sport;
    private Integer order;
    @Builder.Default
    private List<Event> events = new ArrayList<>();

    @Override
    public String toString() {
        return caption + "\n"
                + eventsToString();
    }

    private String eventsToString() {
        StringBuilder builder = new StringBuilder();
        for (Event event : events) {
            builder
                    .append("\t")
                    .append(event.toString());
        }
        return builder.toString();
    }
}
