package com.thoughtworks.mindit.mindit.view.model;

import com.thoughtworks.mindit.mindit.Constants;

import java.util.ArrayList;

public class UINode {
    private String id;
    private String name;
    private int depth;
    private String status;
    private ArrayList<UINode> childSubTree;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    private String parentId;

    public UINode(String name, int depth, String parentId) {
        this.id = "";
        this.name = name;
        this.depth = depth;
        this.status = Constants.STATUS.COLLAPSE.toString();
        this.parentId = parentId;
        childSubTree = new ArrayList<UINode>();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setChildSubTree(ArrayList<UINode> childSubTree) {
        this.childSubTree = childSubTree;
    }

    public ArrayList<UINode> getChildSubTree() {
        return this.childSubTree;
    }

    public void toggleStatus() {
        if (status.equalsIgnoreCase(Constants.STATUS.EXPAND.toString())) {
            status = Constants.STATUS.COLLAPSE.toString();
        } else {
            status = Constants.STATUS.EXPAND.toString();
        }
    }

    public boolean isExpanded() {
        return status.equalsIgnoreCase(Constants.STATUS.EXPAND.toString());
    }

    public void removeChild(UINode uiNode) {
        ArrayList<UINode> childSubTree = this.getChildSubTree();
        childSubTree.remove(uiNode);
        if (childSubTree.size() == 0) {
            this.setStatus(Constants.STATUS.COLLAPSE.toString());
        } else
            this.setStatus(Constants.STATUS.EXPAND.toString());
    }
}
