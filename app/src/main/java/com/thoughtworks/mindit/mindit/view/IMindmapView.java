package com.thoughtworks.mindit.mindit.view;

import com.thoughtworks.mindit.mindit.view.model.UINode;

/**
 * Created by sjadhav on 04/02/16.
 */
public interface IMindmapView {
    public void notifyDataChanged();

    public void updateChildTree(UINode existingParent);
}
