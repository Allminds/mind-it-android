package com.thoughtworks.mindit.mindit;

public class Constants {
    public static final int PADDING_FOR_DEPTH = 20;
    public static final int HEIGHT_DIVIDER = 15;
    public enum POSITION {
        LEFT("left"),
        RIGHT("right");

        private final String name;

        private POSITION(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
    public enum STATUS {
        EXPAND("expand"),
        COLLAPSE("collapse");

        private final String value;

        private STATUS(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }
    }
    public enum TREE_UPDATE_OPTIONS {
        ADD(1),
        UPDATE(2),
        DELETE(3);

        private final int value;

        private TREE_UPDATE_OPTIONS(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
