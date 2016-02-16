package com.thoughtworks.mindit.constant;

public enum UpdateOption {
    ADD(1),
    UPDATE(2),
    DELETE(3);

    private final int value;

    UpdateOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}