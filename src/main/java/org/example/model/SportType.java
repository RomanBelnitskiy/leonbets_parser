package org.example.model;

public enum SportType {
    FOOTBALL("Football"),
    ICE_HOCKEY("Ice Hockey"),
    TENNIS("Tennis"),
    BASKETBALL("Basketball");

    private final String value;

    SportType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
