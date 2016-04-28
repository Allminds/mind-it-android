package com.thoughtworks.mindit.constant;

/**
 * Created by sjadhav on 25/04/16.
 */
public enum LinkType {
    READ_ONLY_LINK("readOnlyLink"),
    READ_WRITE_LINK("readWriteLink");

    private final String name;

    LinkType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
