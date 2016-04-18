package com.thoughtworks.mindit.helper;

import com.thoughtworks.mindit.model.Node;

import java.util.ArrayList;

public interface OnMindmapsLoaded {
    public void onMindmapsLoaded(ArrayList<Node> rootNodes);
    public void onLoadingError(Error error);
}
