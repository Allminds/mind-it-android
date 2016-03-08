package com.thoughtworks.mindit.constant;

public enum Operation {
    OPEN("open"),
    CREATE("create");

    private final String name;

    Operation(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
