package com.thoughtworks.mindit.mindit.view.model;

import com.thoughtworks.mindit.mindit.Constants;

import java.util.ArrayList;

public class UINode {
    private String id;
    private String name;
    private int depth;
    private String status;
    private ArrayList<UINode> childSubTree;
    private String parentId;

    public UINode(String name, int depth, String parentId) {
        this.id = "";
        this.name = name;
        this.depth = depth;
        this.status = Constants.STATUS.COLLAPSE.toString();
        this.parentId = parentId;
        childSubTree = new ArrayList<UINode>();
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    public ArrayList<UINode> getChildSubTree() {
        return this.childSubTree;
    }

    public void setChildSubTree(ArrayList<UINode> childSubTree) {
        this.childSubTree = childSubTree;
    }

    public String toggleStatus() {
        if (status.equalsIgnoreCase(Constants.STATUS.EXPAND.toString())) {
            status = Constants.STATUS.COLLAPSE.toString();
        } else {
            status = Constants.STATUS.EXPAND.toString();
        }
        return status;
    }

    public boolean isExpanded() {
        return status.equalsIgnoreCase(Constants.STATUS.EXPAND.toString());
    }

    public boolean removeChild(UINode uiNode) {
        ArrayList<UINode> childSubTree = this.getChildSubTree();
        boolean result = childSubTree.remove(uiNode);
        if (childSubTree.size() == 0) {
            this.setStatus(Constants.STATUS.COLLAPSE.toString());
        } else
            this.setStatus(Constants.STATUS.EXPAND.toString());
        return result;
    }

    public boolean addChild(UINode uiNode) {
        ArrayList<UINode> parentChildSubTree = this.getChildSubTree();
        parentChildSubTree.add(this.getChildSubTree().size(), uiNode);
        return parentChildSubTree.contains(uiNode);
    }

    @Override
    public String toString() {
        return "UINode{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", childSubTree=" + childSubTree +
                ", parentId='" + parentId + '\'' +
                '}';
    }
}
