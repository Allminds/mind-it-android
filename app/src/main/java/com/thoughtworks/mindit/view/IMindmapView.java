package com.thoughtworks.mindit.view;

import com.thoughtworks.mindit.view.model.UINode;


public interface IMindmapView {
    void notifyDataChanged();

    void updateChildTree(UINode existingParent);
}
