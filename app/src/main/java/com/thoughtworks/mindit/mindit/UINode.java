package com.thoughtworks.mindit.mindit;

import java.util.ArrayList;

public class UINode
{
    private String id;
    private String name;
    private int depth;
    private String status;
    private ArrayList<UINode>childSubTree;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    private String parentId;
    public UINode(String name, int depth,String parentId)
    {
        this.id="";
        this.name=name;
        this.depth=depth;
        this.status="collapse";
        this.parentId=parentId;
        childSubTree=new ArrayList<UINode>();

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

    public ArrayList<UINode> getChildSubTree(){
        return this.childSubTree;
    }

    public void toggleStatus() {
        if(status.equalsIgnoreCase("expand")){
            status="collapse";
        }
        else {
            status="expand";
        }
    }

    public boolean isExpanded() {
        return status.equalsIgnoreCase("expand");
    }


}
