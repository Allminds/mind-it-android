package com.thoughtworks.mindit.authentication;

public class MindmapRequest {

    private String id;
    private boolean responded;

    public String getId() {
        return id;
    }

    public boolean isResponded() {
        return responded;
    }

    public MindmapRequest(String id,boolean responded) {
        this.id = id;
        this.responded = responded;
    }

    public boolean setResponded(boolean responded){
        this.responded = responded;
        return responded;
    }
}
