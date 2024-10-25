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
public class Market {
    private String name;
    @Builder.Default
    private List<String> runners = new ArrayList<>();

    @Override
    public String toString() {
        return name + "\n"
                + runnersToString();
    }

    private String runnersToString() {
        StringBuilder builder = new StringBuilder();
        for (String runner : runners) {
            builder
                    .append("\t\t\t")
                    .append(runner)
                    .append("\n");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Market market = (Market) o;
        return name.equals(market.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
