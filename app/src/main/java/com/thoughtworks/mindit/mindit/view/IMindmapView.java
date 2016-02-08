package com.thoughtworks.mindit.mindit.view;

import com.thoughtworks.mindit.mindit.view.model.UINode;


public interface IMindmapView {
    void notifyDataChanged();

    void updateChildTree(UINode existingParent);
}
