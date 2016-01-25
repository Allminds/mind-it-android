package com.thoughtworks.mindit.mindit;

public class Constants {
    public static final int PADDING_FOR_DEPTH = 20;
    public static final int HEIGHT_DIVIDER = 15;

    public enum POSITION {
        LEFT("left"),
        RIGHT("right");

        private final String name;

        private POSITION(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }
}
